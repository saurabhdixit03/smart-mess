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
import com.smartmess.backend.repository.MessRepository;
import com.smartmess.backend.security.CustomerSecurity;
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
    private final MessRepository messRepository;
    private final CustomerSecurity customerSecurity;

    public MessClosureServiceImpl(
            MessClosureRepository messClosureRepository,
            MenuRepository menuRepository,
            MessClosureMapper messClosureMapper,
            NotificationService notificationService,
            Clock clock,
            MessRepository messRepository,
            CustomerSecurity customerSecurity) {

        this.messClosureRepository = messClosureRepository;
        this.menuRepository = menuRepository;
        this.messClosureMapper = messClosureMapper;
        this.notificationService = notificationService;
        this.clock = clock;
        this.messRepository = messRepository;
        this.customerSecurity = customerSecurity;
    }

    @Override
    public MessClosureResponse createClosure(
            CreateMessClosureRequest request) {

        Long messId =
                customerSecurity.getCurrentMessId();

        validateDateRange(
                request.startDate(),
                request.startSession(),
                request.endDate(),
                request.endSession()
        );

        ClosureBoundary effectiveStart =
                resolveEffectiveStart(
                        messId,
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
                messId,
                null,
                effectiveStart.date(),
                effectiveStart.session(),
                request.endDate(),
                request.endSession()
        );

        MessClosure closure =
                messClosureMapper.toEntity(request);

        closure.setMess(
                messRepository
                        .findById(messId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Mess not found."
                                ))
        );

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
         *
         * Recipients are restricted to the authenticated mess.
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

        Long messId =
                customerSecurity.getCurrentMessId();

        MessClosure closure =
                findClosure(closureId, messId);

        validateDateRange(
                request.startDate(),
                request.startSession(),
                request.endDate(),
                request.endSession()
        );

        ClosureBoundary effectiveStart =
                resolveEffectiveStart(
                        messId,
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
                messId,
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
         *
         * Recipients are restricted to the authenticated mess.
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

        Long messId =
                customerSecurity.getCurrentMessId();

        MessClosure closure =
                findClosure(closureId, messId);

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
         *
         * Recipients are restricted to the authenticated mess.
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

        Long messId =
                customerSecurity.getCurrentMessId();

        LocalDate today =
                LocalDate.now(clock);

        return messClosureRepository
                .findByMess_MessIdAndEndDateGreaterThanEqualOrderByStartDateAsc(
                        messId,
                        today
                )
                .stream()
                .map(messClosureMapper::toResponse)
                .toList();
    }

    @Override
    public List<MessClosureResponse>
            getClosureHistory() {

        Long messId =
                customerSecurity.getCurrentMessId();

        return messClosureRepository
                .findAllByMess_MessIdOrderByStartDateDesc(
                        messId
                )
                .stream()
                .map(messClosureMapper::toResponse)
                .toList();
    }

    /*
     * Resolves a closure only within the authenticated mess.
     */
    private MessClosure findClosure(
            Long closureId,
            Long messId) {

        return messClosureRepository
                .findByClosureIdAndMess_MessId(
                        closureId,
                        messId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Mess closure not found with ID: "
                                        + closureId
                        ));
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

    /*
     * Preserves the existing effective-start rules.
     *
     * Published sessions are checked within this mess only.
     */
    private ClosureBoundary resolveEffectiveStart(
            Long messId,
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
                            .existsByMess_MessIdAndMenuDateAndMealSession(
                                    messId,
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
                            .existsByMess_MessIdAndMenuDateAndMealSession(
                                    messId,
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
                        .existsByMess_MessIdAndMenuDateAndMealSession(
                                messId,
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
            Long messId,
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

        /*
         * Select date-overlap candidates from this mess only.
         *
         * The inclusive meal-slot comparison below remains
         * the final overlap check.
         */
        List<MessClosure> existingClosures =
                messClosureRepository
                        .findByMess_MessIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                                messId,
                                endDate,
                                startDate
                        );

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
    ) {
    }
}