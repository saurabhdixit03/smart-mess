package com.smartmess.backend.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.smartmess.backend.dto.request.CreateMenuRequest;
import com.smartmess.backend.dto.response.MenuAvailabilityResponse;
import com.smartmess.backend.dto.response.MenuResponse;
import com.smartmess.backend.entity.Menu;
import com.smartmess.backend.entity.MessClosure;
import com.smartmess.backend.entity.MessSettings;
import com.smartmess.backend.enums.MealSession;
import com.smartmess.backend.enums.NotificationType;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.exception.ResourceNotFoundException;
import com.smartmess.backend.mapper.MenuMapper;
import com.smartmess.backend.repository.MenuRepository;
import com.smartmess.backend.repository.MessClosureRepository;
import com.smartmess.backend.repository.MessSettingsRepository;
import com.smartmess.backend.service.MenuService;
import com.smartmess.backend.service.NotificationService;

@Service
public class MenuServiceImpl
        implements MenuService {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a");

    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;
    private final MessSettingsRepository messSettingsRepository;
    private final MessClosureRepository messClosureRepository;
    private final NotificationService notificationService;
    private final Clock clock;

    public MenuServiceImpl(
            MenuRepository menuRepository,
            MenuMapper menuMapper,
            MessSettingsRepository messSettingsRepository,
            MessClosureRepository messClosureRepository,
            NotificationService notificationService,
            Clock clock) {

        this.menuRepository = menuRepository;
        this.menuMapper = menuMapper;
        this.messSettingsRepository = messSettingsRepository;
        this.messClosureRepository = messClosureRepository;
        this.notificationService = notificationService;
        this.clock = clock;
    }

    @Override
    public MenuResponse publishMenu(
            CreateMenuRequest request) {

        validateMenuDate(request);
        validateMenuNotPublished(request);
        validateWeeklySchedule(request);
        validateTemporaryClosure(request);
        validateResponseCutoff(request);

        Menu menu =
                menuMapper.toEntity(request);

        Menu savedMenu =
                menuRepository.save(menu);

        /*
         * Notify active customers only after
         * the menu has been published successfully.
         */
        notificationService.notifyActiveCustomers(
                NotificationType.MENU_PUBLISHED,
                "Menu Published",
                buildMenuNotificationMessage(savedMenu)
        );

        return menuMapper.toResponse(savedMenu);
    }

    @Override
    public List<MenuResponse> getTodayMenus() {

        List<Menu> menus =
                menuRepository
                        .findByMenuDateOrderByMealSessionAsc(
                                LocalDate.now(clock)
                        );

        return menus.stream()
                .map(menuMapper::toResponse)
                .toList();
    }

    @Override
    public List<MenuAvailabilityResponse>
            getTodayMenuAvailability() {

        List<MenuAvailabilityResponse> availability =
                new ArrayList<>();

        availability.add(
                buildMenuAvailability(
                        MealSession.LUNCH
                )
        );

        availability.add(
                buildMenuAvailability(
                        MealSession.DINNER
                )
        );

        return availability;
    }

    @Override
    public MenuResponse getMenuById(
            Long menuId) {

        Menu menu =
                menuRepository
                        .findById(menuId)
                        .orElseThrow(() ->
                                new BusinessException(
                                        "Menu not found with ID: "
                                                + menuId
                                )
                        );

        return menuMapper.toResponse(menu);
    }

    @Override
    public List<MenuResponse> getMenuHistory() {

        List<Menu> menus =
                menuRepository
                        .findAllByOrderByMenuDateDescMealSessionAsc();

        return menus.stream()
                .map(menuMapper::toResponse)
                .toList();
    }

    private void validateMenuDate(
            CreateMenuRequest request) {

        LocalDate today =
                LocalDate.now(clock);

        if (!today.equals(request.menuDate())) {
            throw new BusinessException(
                    "Menu can only be published for today."
            );
        }
    }

    private void validateMenuNotPublished(
            CreateMenuRequest request) {

        if (menuRepository.existsByMenuDateAndMealSession(
                request.menuDate(),
                request.mealSession())) {

            throw new BusinessException(
                    "Menu already published for this date and meal session."
            );
        }
    }

    private void validateWeeklySchedule(
            CreateMenuRequest request) {

        MessSettings settings =
                getMessSettings();

        if (settings.getWeeklyClosedDay() == null) {
            return;
        }

        if (!request.menuDate()
                .getDayOfWeek()
                .equals(settings.getWeeklyClosedDay())) {

            return;
        }

        boolean sessionClosed =
                isWeeklySessionClosed(
                        settings,
                        request.mealSession()
                );

        if (sessionClosed) {
            throw new BusinessException(
                    "Menu cannot be published because this meal session is closed as per the weekly schedule."
            );
        }
    }

    private void validateTemporaryClosure(
            CreateMenuRequest request) {

        MessClosure closure =
                findBlockingClosure(
                        request.menuDate(),
                        request.mealSession()
                );

        if (closure != null) {
            throw new BusinessException(
                    "Menu cannot be published because this meal session is temporarily closed."
            );
        }
    }

    /*
     * Prevents menu publishing once the response
     * cutoff for that meal session has passed.
     */
    private void validateResponseCutoff(
            CreateMenuRequest request) {

        MessSettings settings =
                getMessSettings();

        LocalTime cutoffTime =
                getResponseCutoff(
                        settings,
                        request.mealSession()
                );

        if (cutoffTime == null) {
            throw new BusinessException(
                    "Response cutoff time is not configured for this meal session."
            );
        }

        LocalTime currentTime =
                LocalTime.now(clock);

        if (!currentTime.isBefore(cutoffTime)) {
            throw new BusinessException(
                    "Menu cannot be published because the response cutoff passed at "
                            + cutoffTime.format(TIME_FORMAT)
                            + "."
            );
        }
    }

    /*
     * Builds the current publishing state for one
     * meal session using the same backend business rules.
     */
    private MenuAvailabilityResponse buildMenuAvailability(
            MealSession mealSession) {

        LocalDate today =
                LocalDate.now(clock);

        if (menuRepository.existsByMenuDateAndMealSession(
                today,
                mealSession)) {

            return new MenuAvailabilityResponse(
                    mealSession,
                    false,
                    "Menu already published."
            );
        }

        MessSettings settings =
                getMessSettings();

        if (settings.getWeeklyClosedDay() != null
                && today.getDayOfWeek()
                        .equals(settings.getWeeklyClosedDay())
                && isWeeklySessionClosed(
                        settings,
                        mealSession)) {

            return new MenuAvailabilityResponse(
                    mealSession,
                    false,
                    "Closed as per the weekly schedule."
            );
        }

        MessClosure closure =
                findBlockingClosure(
                        today,
                        mealSession
                );

        if (closure != null) {
            return new MenuAvailabilityResponse(
                    mealSession,
                    false,
                    "Temporarily closed · "
                            + closure.getReason()
            );
        }

        LocalTime cutoffTime =
                getResponseCutoff(
                        settings,
                        mealSession
                );

        if (cutoffTime == null) {
            return new MenuAvailabilityResponse(
                    mealSession,
                    false,
                    "Response cutoff is not configured."
            );
        }

        LocalTime currentTime =
                LocalTime.now(clock);

        if (!currentTime.isBefore(cutoffTime)) {
            return new MenuAvailabilityResponse(
                    mealSession,
                    false,
                    "Response cutoff passed at "
                            + cutoffTime.format(TIME_FORMAT)
                            + "."
            );
        }

        return new MenuAvailabilityResponse(
                mealSession,
                true,
                null
        );
    }

    private MessSettings getMessSettings() {

        return messSettingsRepository
                .findTopByOrderBySettingsIdAsc()
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Mess settings not found."
                        )
                );
    }

    private LocalTime getResponseCutoff(
            MessSettings settings,
            MealSession mealSession) {

        return switch (mealSession) {
            case LUNCH ->
                    settings.getLunchResponseCutoff();

            case DINNER ->
                    settings.getDinnerResponseCutoff();
        };
    }

    private boolean isWeeklySessionClosed(
            MessSettings settings,
            MealSession mealSession) {

        return switch (mealSession) {
            case LUNCH ->
                    settings.isWeeklyLunchClosed();

            case DINNER ->
                    settings.isWeeklyDinnerClosed();
        };
    }

    private MessClosure findBlockingClosure(
            LocalDate date,
            MealSession mealSession) {

        List<MessClosure> closures =
                messClosureRepository
                        .findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
                                date,
                                date
                        );

        long requestedSlot =
                toSlot(
                        date,
                        mealSession
                );

        for (MessClosure closure : closures) {

            long closureStart =
                    toSlot(
                            closure.getStartDate(),
                            closure.getStartSession()
                    );

            long closureEnd =
                    toSlot(
                            closure.getEndDate(),
                            closure.getEndSession()
                    );

            if (requestedSlot >= closureStart
                    && requestedSlot <= closureEnd) {

                return closure;
            }
        }

        return null;
    }

    /*
     * Builds the customer-facing notification message
     * after a menu has been published successfully.
     */
    private String buildMenuNotificationMessage(
            Menu menu) {

        String session =
                switch (menu.getMealSession()) {
                    case LUNCH -> "Lunch";
                    case DINNER -> "Dinner";
                };

        MessSettings settings =
                getMessSettings();

        LocalTime cutoffTime =
                getResponseCutoff(
                        settings,
                        menu.getMealSession()
                );

        if (cutoffTime == null) {
            return session
                    + " menu is now available.";
        }

        return session
                + " menu is available · Respond by "
                + cutoffTime.format(TIME_FORMAT);
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
}