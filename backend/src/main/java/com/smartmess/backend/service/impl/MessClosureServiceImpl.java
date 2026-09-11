package com.smartmess.backend.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.request.CreateMessClosureRequest;
import com.smartmess.backend.dto.request.UpdateMessClosureRequest;
import com.smartmess.backend.dto.response.MessClosureResponse;
import com.smartmess.backend.entity.MessClosure;
import com.smartmess.backend.enums.MealSession;
import com.smartmess.backend.enums.NotificationType;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.mapper.MessClosureMapper;
import com.smartmess.backend.repository.MenuRepository;
import com.smartmess.backend.repository.MessClosureRepository;
import com.smartmess.backend.service.MessClosureService;
import com.smartmess.backend.service.NotificationService;

@Service
public class MessClosureServiceImpl
        implements MessClosureService {

    private static final DateTimeFormatter NOTIFICATION_DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM");

    private final MessClosureRepository messClosureRepository;
    private final MenuRepository menuRepository;
    private final MessClosureMapper messClosureMapper;
    private final NotificationService notificationService;
    private final Clock clock;

    public MessClosureServiceImpl(
            MessClosureRepository messClosureRepository,
            MenuRepository menuRepository,
            MessClosureMapper messClosureMapper,
            NotificationService notificationService,
            Clock clock) {

        this.messClosureRepository = messClosureRepository;
        this.menuRepository = menuRepository;
        this.messClosureMapper = messClosureMapper;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    @Override
    public MessClosureResponse createClosure(
            CreateMessClosureRequest request) {

        validateDateRange(
                request.startDate(),
                request.startSession(),
                request.endDate(),
                request.endSession()
        );

        ClosureBoundary effectiveStart =
                resolveEffectiveStart(
                        request.startDate(),
                        request.startSession()
                );

        validateEffectiveRange(
                effectiveStart.date(),
                effectiveStart.session(),
                request.endDate(),
                request.endSession()
        );

        validateNoOverlap(
                null,
                effectiveStart.date(),
                effectiveStart.session(),
                request.endDate(),
                request.endSession()
        );

        MessClosure closure =
                messClosureMapper.toEntity(request);

        closure.setStartDate(
                effectiveStart.date()
        );

        closure.setStartSession(
                effectiveStart.session()
        );

        MessClosure savedClosure =
                messClosureRepository.save(
                        closure
                );

        /*
         * Notify active customers only after
         * the temporary closure has been created successfully.
         */
        notificationService.notifyActiveCustomers(
                NotificationType.MESS_CLOSURE,
                "Mess Temporarily Closed",
                buildClosureNotificationMessage(
                        savedClosure
                )
        );

        return messClosureMapper.toResponse(
                savedClosure
        );
    }

    @Override
    public MessClosureResponse updateClosure(
            Long closureId,
            UpdateMessClosureRequest request) {

        MessClosure closure =
                messClosureRepository
                        .findById(closureId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Mess closure not found with ID: "
                                                + closureId
                                )
                        );

        validateDateRange(
                request.startDate(),
                request.startSession(),
                request.endDate(),
                request.endSession()
        );

        ClosureBoundary effectiveStart =
                resolveEffectiveStart(
                        request.startDate(),
                        request.startSession()
                );

        validateEffectiveRange(
                effectiveStart.date(),
                effectiveStart.session(),
                request.endDate(),
                request.endSession()
        );

        validateNoOverlap(
                closureId,
                effectiveStart.date(),
                effectiveStart.session(),
                request.endDate(),
                request.endSession()
        );

        boolean changed =
                !Objects.equals(
                        closure.getStartDate(),
                        effectiveStart.date()
                )
                        || closure.getStartSession()
                                != effectiveStart.session()
                        || !Objects.equals(
                                closure.getEndDate(),
                                request.endDate()
                        )
                        || closure.getEndSession()
                                != request.endSession()
                        || !Objects.equals(
                                closure.getReason(),
                                request.reason()
                        );

        if (!changed) {
            return messClosureMapper.toResponse(
                    closure
            );
        }

        messClosureMapper.updateClosureFromRequest(
                request,
                closure
        );

        closure.setStartDate(
                effectiveStart.date()
        );

        closure.setStartSession(
                effectiveStart.session()
        );

        MessClosure updatedClosure =
                messClosureRepository.save(
                        closure
                );

        /*
         * Notify active customers only after
         * the temporary closure has actually changed.
         */
        notificationService.notifyActiveCustomers(
                NotificationType.MESS_CLOSURE,
                "Closure Updated",
                buildClosureNotificationMessage(
                        updatedClosure
                )
        );

        return messClosureMapper.toResponse(
                updatedClosure
        );
    }

    @Override
    public void deleteClosure(
            Long closureId) {

        MessClosure closure =
                messClosureRepository
                        .findById(closureId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Mess closure not found with ID: "
                                                + closureId
                                )
                        );

        /*
         * Build the customer-facing details before
         * deleting the closure.
         */
        String notificationMessage =
                buildClosureNotificationMessage(
                        closure
                );

        messClosureRepository.delete(
                closure
        );

        /*
         * Notify active customers only after
         * the temporary closure has been cancelled successfully.
         */
        notificationService.notifyActiveCustomers(
                NotificationType.MESS_CLOSURE,
                "Closure Cancelled",
                notificationMessage
        );
    }

    @Override
    public List<MessClosureResponse>
            getCurrentAndUpcomingClosures() {

        LocalDate today =
                LocalDate.now(clock);

        return messClosureRepository
                .findByEndDateGreaterThanEqualOrderByStartDateAsc(
                        today
                )
                .stream()
                .map(messClosureMapper::toResponse)
                .toList();
    }

    @Override
    public List<MessClosureResponse>
            getClosureHistory() {

        return messClosureRepository
                .findAllByOrderByStartDateDesc()
                .stream()
                .map(messClosureMapper::toResponse)
                .toList();
    }

    private void validateDateRange(
            LocalDate startDate,
            MealSession startSession,
            LocalDate endDate,
            MealSession endSession) {

        LocalDate today =
                LocalDate.now(clock);

        if (startDate.isBefore(today)) {
            throw new BusinessException(
                    "Closure start date cannot be in the past."
            );
        }

        if (endDate.isBefore(startDate)) {
            throw new BusinessException(
                    "Closure end date cannot be before the start date."
            );
        }

        if (startDate.equals(endDate)
                && sessionOrder(startSession)
                > sessionOrder(endSession)) {

            throw new BusinessException(
                    "Closure end session cannot be before the start session on the same date."
            );
        }
    }

    private ClosureBoundary resolveEffectiveStart(
            LocalDate startDate,
            MealSession startSession) {

        LocalDate today =
                LocalDate.now(clock);

        if (!startDate.equals(today)) {
            return new ClosureBoundary(
                    startDate,
                    startSession
            );
        }

        if (startSession == MealSession.LUNCH) {

            boolean lunchPublished =
                    menuRepository
                            .existsByMenuDateAndMealSession(
                                    today,
                                    MealSession.LUNCH
                            );

            if (!lunchPublished) {
                return new ClosureBoundary(
                        today,
                        MealSession.LUNCH
                );
            }

            boolean dinnerPublished =
                    menuRepository
                            .existsByMenuDateAndMealSession(
                                    today,
                                    MealSession.DINNER
                            );

            if (!dinnerPublished) {
                return new ClosureBoundary(
                        today,
                        MealSession.DINNER
                );
            }

            return new ClosureBoundary(
                    today.plusDays(1),
                    MealSession.LUNCH
            );
        }

        boolean dinnerPublished =
                menuRepository
                        .existsByMenuDateAndMealSession(
                                today,
                                MealSession.DINNER
                        );

        if (!dinnerPublished) {
            return new ClosureBoundary(
                    today,
                    MealSession.DINNER
            );
        }

        return new ClosureBoundary(
                today.plusDays(1),
                MealSession.LUNCH
        );
    }

    private void validateEffectiveRange(
            LocalDate startDate,
            MealSession startSession,
            LocalDate endDate,
            MealSession endSession) {

        long startSlot =
                toSlot(
                        startDate,
                        startSession
                );

        long endSlot =
                toSlot(
                        endDate,
                        endSession
                );

        if (startSlot > endSlot) {
            throw new BusinessException(
                    "No meal session is available to close within the requested range."
            );
        }
    }

    private void validateNoOverlap(
            Long currentClosureId,
            LocalDate startDate,
            MealSession startSession,
            LocalDate endDate,
            MealSession endSession) {

        long requestedStart =
                toSlot(
                        startDate,
                        startSession
                );

        long requestedEnd =
                toSlot(
                        endDate,
                        endSession
                );

        List<MessClosure> existingClosures =
                messClosureRepository.findAll();

        for (MessClosure existing : existingClosures) {

            if (currentClosureId != null
                    && existing.getClosureId()
                            .equals(currentClosureId)) {

                continue;
            }

            long existingStart =
                    toSlot(
                            existing.getStartDate(),
                            existing.getStartSession()
                    );

            long existingEnd =
                    toSlot(
                            existing.getEndDate(),
                            existing.getEndSession()
                    );

            boolean overlaps =
                    requestedStart <= existingEnd
                            && requestedEnd >= existingStart;

            if (overlaps) {
                throw new BusinessException(
                        "The requested closure overlaps with an existing mess closure."
                );
            }
        }
    }

    /*
     * Builds a concise customer-facing notification.
     *
     * Title explains the event,
     * while the message contains only the useful details.
     */
    private String buildClosureNotificationMessage(
            MessClosure closure) {

        LocalDate startDate =
                closure.getStartDate();

        LocalDate endDate =
                closure.getEndDate();

        MealSession startSession =
                closure.getStartSession();

        MealSession endSession =
                closure.getEndSession();

        String startDateText =
                startDate.format(
                        NOTIFICATION_DATE_FORMAT
                );

        String endDateText =
                endDate.format(
                        NOTIFICATION_DATE_FORMAT
                );

        String closurePeriod;

        /*
         * Full-day closure on one date.
         */
        if (startDate.equals(endDate)
                && startSession == MealSession.LUNCH
                && endSession == MealSession.DINNER) {

            closurePeriod =
                    startDateText;

        /*
         * Only one meal session is closed.
         */
        } else if (startDate.equals(endDate)
                && startSession == endSession) {

            closurePeriod =
                    formatSession(startSession)
                            + " on "
                            + startDateText;

        /*
         * Full closure across multiple dates.
         */
        } else if (startSession == MealSession.LUNCH
                && endSession == MealSession.DINNER) {

            closurePeriod =
                    startDateText
                            + " to "
                            + endDateText;

        /*
         * Closure starts or ends at a specific meal session.
         */
        } else {

            closurePeriod =
                    formatSession(startSession)
                            + " on "
                            + startDateText
                            + " to "
                            + formatSession(endSession)
                            + " on "
                            + endDateText;
        }

        return closurePeriod
                + " · "
                + closure.getReason();
    }

    private String formatSession(
            MealSession session) {

        return switch (session) {
            case LUNCH -> "Lunch";
            case DINNER -> "Dinner";
        };
    }

    private long toSlot(
            LocalDate date,
            MealSession session) {

        return date.toEpochDay() * 2
                + sessionOrder(session);
    }

    private int sessionOrder(
            MealSession session) {

        return switch (session) {
            case LUNCH -> 0;
            case DINNER -> 1;
        };
    }

    private record ClosureBoundary(
            LocalDate date,
            MealSession session
    ) {}
}