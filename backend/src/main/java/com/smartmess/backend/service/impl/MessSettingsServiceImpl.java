package com.smartmess.backend.service.impl;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.request.UpdateResponseWindowRequest;
import com.smartmess.backend.dto.request.UpdateWeeklyScheduleRequest;
import com.smartmess.backend.dto.response.MessSettingsResponse;
import com.smartmess.backend.entity.Mess;
import com.smartmess.backend.entity.MessSettings;
import com.smartmess.backend.enums.NotificationType;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.mapper.MessSettingsMapper;
import com.smartmess.backend.repository.MessRepository;
import com.smartmess.backend.repository.MessSettingsRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.MessSettingsService;
import com.smartmess.backend.service.NotificationService;
import java.util.Locale;
@Service
public class MessSettingsServiceImpl
        implements MessSettingsService {

	private static final DateTimeFormatter NOTIFICATION_TIME_FORMAT =
	        DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    private final MessSettingsRepository messSettingsRepository;
    private final MessSettingsMapper messSettingsMapper;
    private final NotificationService notificationService;
    private final MessRepository messRepository;
    private final CustomerSecurity customerSecurity;

    public MessSettingsServiceImpl(
            MessSettingsRepository messSettingsRepository,
            MessSettingsMapper messSettingsMapper,
            NotificationService notificationService,
            MessRepository messRepository,
            CustomerSecurity customerSecurity) {

        this.messSettingsRepository = messSettingsRepository;
        this.messSettingsMapper = messSettingsMapper;
        this.notificationService = notificationService;
        this.messRepository = messRepository;
        this.customerSecurity = customerSecurity;
    }

    @Override
    public MessSettingsResponse getSettings() {

        Long messId =
                customerSecurity.getCurrentMessId();

        MessSettings settings =
                messSettingsRepository
                        .findByMess_MessId(messId)
                        .orElseGet(MessSettings::new);

        return messSettingsMapper.toResponse(
                settings
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
                messSettingsRepository.save(settings);

        /*
         * Notify active customers of this mess only after
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
                messSettingsRepository.save(settings);

        /*
         * Notify active customers of this mess only after
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

    /*
     * Returns the authenticated mess's existing settings,
     * or prepares a new settings record owned by that mess.
     *
     * Persistence remains the responsibility of the caller.
     */
    private MessSettings getOrCreateSettings() {

        Long messId =
                customerSecurity.getCurrentMessId();

        return messSettingsRepository
                .findByMess_MessId(messId)
                .orElseGet(() -> {

                    MessSettings settings =
                            new MessSettings();

                    settings.setMess(
                            getMess(messId)
                    );

                    return settings;
                });
    }

    /*
     * Resolves tenant ownership from the authenticated mess ID.
     * Requests cannot supply a different mess for settings updates.
     */
    private Mess getMess(Long messId) {

        return messRepository
                .findById(messId)
                .orElseThrow(() ->
                        new BusinessException(
                                "Mess not found."
                        ));
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
                formatDayOfWeek(closedDay);

        if (settings.isWeeklyLunchClosed()
                && settings.isWeeklyDinnerClosed()) {

            return day + " • Lunch & Dinner closed";
        }

        if (settings.isWeeklyLunchClosed()) {
            return day + " • Lunch closed";
        }

        return day + " • Dinner closed";
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
                dayOfWeek.name().toLowerCase();

        return Character.toUpperCase(
                value.charAt(0)
        ) + value.substring(1);
    }
}