package com.smartmess.backend.service.impl;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.dto.request.CreateMenuRequest;
import com.smartmess.backend.dto.response.MenuAvailabilityResponse;
import com.smartmess.backend.dto.response.MenuResponse;
import com.smartmess.backend.entity.Dish;
import com.smartmess.backend.entity.Menu;
import com.smartmess.backend.entity.MessClosure;
import com.smartmess.backend.entity.MessSettings;
import com.smartmess.backend.enums.DishCategory;
import com.smartmess.backend.enums.MealSession;
import com.smartmess.backend.enums.NotificationType;
import com.smartmess.backend.exception.BusinessException;
import com.smartmess.backend.mapper.MenuMapper;
import com.smartmess.backend.repository.MealPricingRepository;
import com.smartmess.backend.repository.MenuRepository;
import com.smartmess.backend.repository.MessClosureRepository;
import com.smartmess.backend.repository.MessRepository;
import com.smartmess.backend.repository.MessSettingsRepository;
import com.smartmess.backend.security.CustomerSecurity;
import com.smartmess.backend.service.DishService;
import com.smartmess.backend.service.MenuService;
import com.smartmess.backend.service.NotificationService;

@Service
public class MenuServiceImpl implements MenuService {

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH);

    private static final String PRICING_REQUIRED_MESSAGE =
            "Configure meal prices in Settings before publishing a menu.";

    private final MenuRepository menuRepository;
    private final MenuMapper menuMapper;
    private final MessSettingsRepository messSettingsRepository;
    private final MessClosureRepository messClosureRepository;
    private final NotificationService notificationService;
    private final Clock clock;
    private final MessRepository messRepository;
    private final CustomerSecurity customerSecurity;
    private final MealPricingRepository mealPricingRepository;

    /*
     * Dish Service
     *
     * Resolves owner-entered dish names into tenant-scoped
     * Dish entities before the Menu is saved.
     */
    private final DishService dishService;

    public MenuServiceImpl(
            MenuRepository menuRepository,
            MenuMapper menuMapper,
            MessSettingsRepository messSettingsRepository,
            MessClosureRepository messClosureRepository,
            NotificationService notificationService,
            Clock clock,
            MessRepository messRepository,
            CustomerSecurity customerSecurity,
            MealPricingRepository mealPricingRepository,
            DishService dishService) {

        this.menuRepository = menuRepository;
        this.menuMapper = menuMapper;
        this.messSettingsRepository = messSettingsRepository;
        this.messClosureRepository = messClosureRepository;
        this.notificationService = notificationService;
        this.clock = clock;
        this.messRepository = messRepository;
        this.customerSecurity = customerSecurity;
        this.mealPricingRepository = mealPricingRepository;
        this.dishService = dishService;
    }

    @Override
    @Transactional
    public MenuResponse publishMenu(
            CreateMenuRequest request) {

        Long messId = customerSecurity.getCurrentMessId();

        validateMenuDate(request);
        validateMenuNotPublished(messId, request);
        validateCurrentPricing(messId);
        validateWeeklySchedule(messId, request);
        validateTemporaryClosure(messId, request);
        validateResponseCutoff(messId, request);

        Menu menu = menuMapper.toEntity(request);

        menu.setMess(
                messRepository
                        .findById(messId)
                        .orElseThrow(() -> new BusinessException(
                                "Mess not found."
                        ))
        );

        /*
         * Resolve the owner-entered dish names into tenant-scoped
         * Dish entities.
         *
         * Categories are determined by the menu slot, so the owner
         * does not need to provide category information.
         */
        Dish sabjiOne = dishService.findOrCreateDish(
                messId,
                request.sabjiOne(),
                DishCategory.SABJI
        );

        Dish sabjiTwo = resolveOptionalDish(
                messId,
                request.sabjiTwo(),
                DishCategory.SABJI
        );

        Dish dal = resolveOptionalDish(
                messId,
                request.dal(),
                DishCategory.DAL
        );

        Dish rice = resolveOptionalDish(
                messId,
                request.rice(),
                DishCategory.RICE
        );

        Dish sweet = resolveOptionalDish(
                messId,
                request.sweet(),
                DishCategory.SWEET
        );

        validateNoDuplicateDish(
                sabjiOne,
                sabjiTwo,
                dal,
                rice,
                sweet
        );

        menu.setSabjiOne(sabjiOne);
        menu.setSabjiTwo(sabjiTwo);
        menu.setDal(dal);
        menu.setRice(rice);
        menu.setSweet(sweet);

        Menu savedMenu = menuRepository.save(menu);

        notificationService.notifyActiveCustomers(
                NotificationType.MENU_PUBLISHED,
                "Menu Published",
                buildMenuNotificationMessage(
                        messId,
                        savedMenu
                )
        );

        return menuMapper.toResponse(savedMenu);
    }

    @Override
    public List<MenuResponse> getTodayMenus() {

        Long messId = customerSecurity.getCurrentMessId();

        return menuRepository
                .findByMess_MessIdAndMenuDateOrderByMealSessionAsc(
                        messId,
                        LocalDate.now(clock)
                )
                .stream()
                .map(menuMapper::toResponse)
                .toList();
    }

    @Override
    public List<MenuAvailabilityResponse> getTodayMenuAvailability() {

        Long messId = customerSecurity.getCurrentMessId();

        List<MenuAvailabilityResponse> availability =
                new ArrayList<>();

        availability.add(
                buildMenuAvailability(
                        messId,
                        MealSession.LUNCH
                )
        );

        availability.add(
                buildMenuAvailability(
                        messId,
                        MealSession.DINNER
                )
        );

        return availability;
    }

    @Override
    public MenuResponse getMenuById(Long menuId) {

        Long messId = customerSecurity.getCurrentMessId();

        Menu menu = menuRepository
                .findByMenuIdAndMess_MessId(
                        menuId,
                        messId
                )
                .orElseThrow(() -> new BusinessException(
                        "Menu not found with ID: " + menuId
                ));

        return menuMapper.toResponse(menu);
    }

    @Override
    public List<MenuResponse> getMenuHistory() {

        Long messId = customerSecurity.getCurrentMessId();

        return menuRepository
                .findAllByMess_MessIdOrderByMenuDateDescMealSessionAsc(
                        messId
                )
                .stream()
                .map(menuMapper::toResponse)
                .toList();
    }

    private void validateMenuDate(
            CreateMenuRequest request) {

        if (!LocalDate.now(clock).equals(request.menuDate())) {
            throw new BusinessException(
                    "Menu can only be published for today."
            );
        }
    }

    private void validateMenuNotPublished(
            Long messId,
            CreateMenuRequest request) {

        if (menuRepository.existsByMess_MessIdAndMenuDateAndMealSession(
                messId,
                request.menuDate(),
                request.mealSession()
        )) {
            throw new BusinessException(
                    "Menu already published for this date and meal session."
            );
        }
    }

    /*
     * Future pricing is excluded.
     * Another mess's pricing cannot satisfy this requirement.
     */
    private boolean hasCurrentPricing(Long messId) {

        return mealPricingRepository
                .findTopByMess_MessIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
                        messId,
                        LocalDateTime.now(clock)
                )
                .isPresent();
    }

    private void validateCurrentPricing(Long messId) {

        if (!hasCurrentPricing(messId)) {
            throw new BusinessException(
                    PRICING_REQUIRED_MESSAGE
            );
        }
    }

    private void validateWeeklySchedule(
            Long messId,
            CreateMenuRequest request) {

        MessSettings settings = getSettingsOrDefaults(messId);

        if (settings.getWeeklyClosedDay() == null
                || !request.menuDate()
                        .getDayOfWeek()
                        .equals(settings.getWeeklyClosedDay())) {
            return;
        }

        if (isWeeklySessionClosed(
                settings,
                request.mealSession()
        )) {
            throw new BusinessException(
                    "Menu cannot be published because this meal session is closed as per the weekly schedule."
            );
        }
    }

    private void validateTemporaryClosure(
            Long messId,
            CreateMenuRequest request) {

        MessClosure closure = findBlockingClosure(
                messId,
                request.menuDate(),
                request.mealSession()
        );

        if (closure != null) {
            throw new BusinessException(
                    "Menu cannot be published because this meal session is temporarily closed."
            );
        }
    }

    private void validateResponseCutoff(
            Long messId,
            CreateMenuRequest request) {

        MessSettings settings = getSettingsOrDefaults(messId);

        LocalTime cutoffTime = getResponseCutoff(
                settings,
                request.mealSession()
        );

        if (cutoffTime == null) {
            throw new BusinessException(
                    "Response cutoff time is not configured for this meal session."
            );
        }

        if (!LocalTime.now(clock).isBefore(cutoffTime)) {
            throw new BusinessException(
                    "Menu cannot be published because the response cutoff passed at "
                            + cutoffTime.format(TIME_FORMAT)
                            + "."
            );
        }
    }

    /*
     * Resolves optional menu items.
     *
     * sabjiOne is required and is resolved directly.
     * The remaining four slots may be null or blank.
     */
    private Dish resolveOptionalDish(
            Long messId,
            String name,
            DishCategory category) {

        if (name == null || name.isBlank()) {
            return null;
        }

        return dishService.findOrCreateDish(
                messId,
                name,
                category
        );
    }

    /*
     * A menu must not contain the same dish more than once.
     *
     * This uses Dish identity rather than raw text, so inputs such as:
     *
     * "Bhindi Masala"
     * "  Bhindi   Masala  "
     *
     * resolve to the same tenant-scoped Dish and are correctly
     * treated as duplicates.
     */
    private void validateNoDuplicateDish(
            Dish sabjiOne,
            Dish sabjiTwo,
            Dish dal,
            Dish rice,
            Dish sweet) {

        Set<Long> dishIds = new HashSet<>();

        List<Dish> dishes = new ArrayList<>();

        dishes.add(sabjiOne);

        if (sabjiTwo != null) {
            dishes.add(sabjiTwo);
        }

        if (dal != null) {
            dishes.add(dal);
        }

        if (rice != null) {
            dishes.add(rice);
        }

        if (sweet != null) {
            dishes.add(sweet);
        }

        for (Dish dish : dishes) {

            if (!dishIds.add(dish.getDishId())) {
                throw new BusinessException(
                        "The same dish cannot be added more than once to the same menu."
                );
            }
        }
    }
    /*
     * Uses the same pricing prerequisite as publication.
     * Already published menus retain their existing availability state.
     */
    private MenuAvailabilityResponse buildMenuAvailability(
            Long messId,
            MealSession mealSession) {

        LocalDate today = LocalDate.now(clock);

        if (menuRepository.existsByMess_MessIdAndMenuDateAndMealSession(
                messId,
                today,
                mealSession
        )) {
            return new MenuAvailabilityResponse(
                    mealSession,
                    false,
                    "Menu already published."
            );
        }

        if (!hasCurrentPricing(messId)) {
            return new MenuAvailabilityResponse(
                    mealSession,
                    false,
                    PRICING_REQUIRED_MESSAGE
            );
        }

        MessSettings settings = getSettingsOrDefaults(messId);

        if (settings.getWeeklyClosedDay() != null
                && today.getDayOfWeek()
                        .equals(settings.getWeeklyClosedDay())
                && isWeeklySessionClosed(
                        settings,
                        mealSession
                )) {
            return new MenuAvailabilityResponse(
                    mealSession,
                    false,
                    "Closed as per the weekly schedule."
            );
        }

        MessClosure closure = findBlockingClosure(
                messId,
                today,
                mealSession
        );

        if (closure != null) {
            return new MenuAvailabilityResponse(
                    mealSession,
                    false,
                    "Temporarily closed · " + closure.getReason()
            );
        }

        LocalTime cutoffTime = getResponseCutoff(
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

        if (!LocalTime.now(clock).isBefore(cutoffTime)) {
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

    private MessSettings getSettingsOrDefaults(Long messId) {

        return messSettingsRepository
                .findByMess_MessId(messId)
                .orElseGet(MessSettings::new);
    }

    private LocalTime getResponseCutoff(
            MessSettings settings,
            MealSession mealSession) {

        return switch (mealSession) {
            case LUNCH -> settings.getLunchResponseCutoff();
            case DINNER -> settings.getDinnerResponseCutoff();
        };
    }

    private boolean isWeeklySessionClosed(
            MessSettings settings,
            MealSession mealSession) {

        return switch (mealSession) {
            case LUNCH -> settings.isWeeklyLunchClosed();
            case DINNER -> settings.isWeeklyDinnerClosed();
        };
    }

    private MessClosure findBlockingClosure(
            Long messId,
            LocalDate date,
            MealSession mealSession) {

        List<MessClosure> closures = messClosureRepository
                .findByMess_MessIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                        messId,
                        date,
                        date
                );

        long requestedSlot = toSlot(date, mealSession);

        for (MessClosure closure : closures) {

            long closureStart = toSlot(
                    closure.getStartDate(),
                    closure.getStartSession()
            );

            long closureEnd = toSlot(
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

    private String buildMenuNotificationMessage(
            Long messId,
            Menu menu) {

        String session = switch (menu.getMealSession()) {
            case LUNCH -> "Lunch";
            case DINNER -> "Dinner";
        };

        MessSettings settings = getSettingsOrDefaults(messId);

        LocalTime cutoffTime = getResponseCutoff(
                settings,
                menu.getMealSession()
        );

        if (cutoffTime == null) {
            return session + " menu is now available.";
        }

        return session
                + " menu is available · Respond by "
                + cutoffTime.format(TIME_FORMAT);
    }

    private long toSlot(
            LocalDate date,
            MealSession session) {

        return date.toEpochDay() * 2 + sessionOrder(session);
    }

    private int sessionOrder(MealSession session) {

        return switch (session) {
            case LUNCH -> 0;
            case DINNER -> 1;
        };
    }
}
