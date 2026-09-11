package com.smartmess.backend.service.impl;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.request.CreateMessSettingsRequest;
import com.smartmess.backend.dto.request.UpdatePaymentSettingsRequest;
import com.smartmess.backend.dto.request.UpdateResponseWindowRequest;
import com.smartmess.backend.dto.request.UpdateWeeklyScheduleRequest;
import com.smartmess.backend.dto.response.MessSettingsResponse;
import com.smartmess.backend.entity.MessSettings;
import com.smartmess.backend.enums.NotificationType;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.mapper.MessSettingsMapper;
import com.smartmess.backend.repository.MessSettingsRepository;
import com.smartmess.backend.service.MessSettingsService;
import com.smartmess.backend.service.NotificationService;

@Service
public class MessSettingsServiceImpl
        implements MessSettingsService {

    private static final DateTimeFormatter NOTIFICATION_TIME_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a");

    private final MessSettingsRepository messSettingsRepository;
    private final MessSettingsMapper messSettingsMapper;
    private final NotificationService notificationService;

    public MessSettingsServiceImpl(
            MessSettingsRepository messSettingsRepository,
            MessSettingsMapper messSettingsMapper,
            NotificationService notificationService) {

        this.messSettingsRepository = messSettingsRepository;
        this.messSettingsMapper = messSettingsMapper;
        this.notificationService = notificationService;
    }

    @Override
    public MessSettingsResponse createSettings(
            CreateMessSettingsRequest request) {

        if (messSettingsRepository
                .findTopByOrderBySettingsIdAsc()
                .isPresent()) {

            throw new BusinessException(
                    "Mess settings already exist. Please update the existing settings."
            );
        }

        MessSettings settings =
                messSettingsMapper.toEntity(request);

        MessSettings savedSettings =
                messSettingsRepository.save(settings);

        return messSettingsMapper.toResponse(
                savedSettings
        );
    }

    @Override
    public MessSettingsResponse getSettings() {

        MessSettings settings =
                messSettingsRepository
                        .findTopByOrderBySettingsIdAsc()
                        .orElseGet(MessSettings::new);

        return messSettingsMapper.toResponse(
                settings
        );
    }

    @Override
    public MessSettingsResponse updatePaymentSettings(
            UpdatePaymentSettingsRequest request) {

        MessSettings settings =
                getOrCreateSettings();

        messSettingsMapper.updateEntityFromRequest(
                request,
                settings
        );

        MessSettings updatedSettings =
                messSettingsRepository.save(
                        settings
                );

        return messSettingsMapper.toResponse(
                updatedSettings
        );
    }

    @Override
    public MessSettingsResponse updateResponseWindow(
            UpdateResponseWindowRequest request) {

        MessSettings settings =
                getOrCreateSettings();

        boolean changed =
                !Objects.equals(
                        settings.getLunchResponseCutoff(),
                        request.lunchResponseCutoff()
                )
                        || !Objects.equals(
                                settings.getDinnerResponseCutoff(),
                                request.dinnerResponseCutoff()
                        );

        if (!changed) {
            return messSettingsMapper.toResponse(
                    settings
            );
        }

        settings.setLunchResponseCutoff(
                request.lunchResponseCutoff()
        );

        settings.setDinnerResponseCutoff(
                request.dinnerResponseCutoff()
        );

        MessSettings updatedSettings =
                messSettingsRepository.save(
                        settings
                );

        /*
         * Notify active customers only after
         * the response window has changed successfully.
         */
        notificationService.notifyActiveCustomers(
                NotificationType.RESPONSE_WINDOW,
                "Response Cutoff Updated",
                buildResponseWindowNotificationMessage(
                        updatedSettings
                )
        );

        return messSettingsMapper.toResponse(
                updatedSettings
        );
    }

    @Override
    public MessSettingsResponse updateWeeklySchedule(
            UpdateWeeklyScheduleRequest request) {

        validateWeeklySchedule(
                request.weeklyClosedDay(),
                request.weeklyLunchClosed(),
                request.weeklyDinnerClosed()
        );

        MessSettings settings =
                getOrCreateSettings();

        boolean changed =
                !Objects.equals(
                        settings.getWeeklyClosedDay(),
                        request.weeklyClosedDay()
                )
                        || settings.isWeeklyLunchClosed()
                                != request.weeklyLunchClosed()
                        || settings.isWeeklyDinnerClosed()
                                != request.weeklyDinnerClosed();

        if (!changed) {
            return messSettingsMapper.toResponse(
                    settings
            );
        }

        settings.setWeeklyClosedDay(
                request.weeklyClosedDay()
        );

        settings.setWeeklyLunchClosed(
                request.weeklyLunchClosed()
        );

        settings.setWeeklyDinnerClosed(
                request.weeklyDinnerClosed()
        );

        MessSettings updatedSettings =
                messSettingsRepository.save(
                        settings
                );

        /*
         * Notify active customers only after
         * the weekly schedule has changed successfully.
         */
        notificationService.notifyActiveCustomers(
                NotificationType.WEEKLY_SCHEDULE,
                "Weekly Schedule Updated",
                buildWeeklyScheduleNotificationMessage(
                        updatedSettings
                )
        );

        return messSettingsMapper.toResponse(
                updatedSettings
        );
    }

    private MessSettings getOrCreateSettings() {

        return messSettingsRepository
                .findTopByOrderBySettingsIdAsc()
                .orElseGet(MessSettings::new);
    }

    private void validateWeeklySchedule(
            DayOfWeek weeklyClosedDay,
            boolean weeklyLunchClosed,
            boolean weeklyDinnerClosed) {

        if (weeklyClosedDay == null) {

            if (weeklyLunchClosed || weeklyDinnerClosed) {
                throw new BusinessException(
                        "A weekly closed day must be selected when a meal session is marked closed."
                );
            }

            return;
        }

        if (!weeklyLunchClosed && !weeklyDinnerClosed) {
            throw new BusinessException(
                    "At least one meal session must be closed for the selected weekly off day."
            );
        }
    }

    /*
     * Builds a concise customer-facing notification
     * with the latest response cutoff times.
     */
    private String buildResponseWindowNotificationMessage(
            MessSettings settings) {

        return "Lunch: "
                + formatNotificationTime(
                        settings.getLunchResponseCutoff()
                )
                + " • Dinner: "
                + formatNotificationTime(
                        settings.getDinnerResponseCutoff()
                );
    }

    /*
     * Builds a concise customer-facing notification
     * for the recurring weekly closure configuration.
     */
    private String buildWeeklyScheduleNotificationMessage(
            MessSettings settings) {

        DayOfWeek closedDay =
                settings.getWeeklyClosedDay();

        if (closedDay == null) {
            return "Weekly closure removed.";
        }

        String day =
                formatDayOfWeek(
                        closedDay
                );

        if (settings.isWeeklyLunchClosed()
                && settings.isWeeklyDinnerClosed()) {

            return day
                    + " • Lunch & Dinner closed";
        }

        if (settings.isWeeklyLunchClosed()) {
            return day
                    + " • Lunch closed";
        }

        return day
                + " • Dinner closed";
    }

    /*
     * Formats LocalTime for customer-facing notifications.
     *
     * Example:
     * 11:30 -> 11:30 AM
     * 18:30 -> 6:30 PM
     */
    private String formatNotificationTime(
            LocalTime time) {

        return time.format(
                NOTIFICATION_TIME_FORMAT
        );
    }

    /*
     * Formats DayOfWeek for customer-facing notifications.
     *
     * Example:
     * SUNDAY -> Sunday
     */
    private String formatDayOfWeek(
            DayOfWeek dayOfWeek) {

        String value =
                dayOfWeek.name()
                        .toLowerCase();

        return Character.toUpperCase(
                value.charAt(0)
        )
                + value.substring(1);
    }
}