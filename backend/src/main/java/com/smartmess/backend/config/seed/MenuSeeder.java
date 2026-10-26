package com.smartmess.backend.config.seed;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.entity.Dish;
import com.smartmess.backend.entity.Mess;
import com.smartmess.backend.entity.Menu;
import com.smartmess.backend.enums.DishCategory;
import com.smartmess.backend.enums.MealSession;
import com.smartmess.backend.repository.DishRepository;
import com.smartmess.backend.repository.MenuRepository;
import com.smartmess.backend.service.DishNameNormalizer;

@Component
public class MenuSeeder {

    private static final Logger log =
            LoggerFactory.getLogger(
                    MenuSeeder.class
            );

    private static final int HISTORICAL_DAY_COUNT =
            43;

    private static final long RANDOM_SEED =
            20260917L;

    private final MenuRepository menuRepository;
    private final DishRepository dishRepository;
    private final DishNameNormalizer dishNameNormalizer;
    private final Clock clock;

    public MenuSeeder(
            MenuRepository menuRepository,
            DishRepository dishRepository,
            DishNameNormalizer dishNameNormalizer,
            Clock clock) {

        this.menuRepository = menuRepository;
        this.dishRepository = dishRepository;
        this.dishNameNormalizer = dishNameNormalizer;
        this.clock = clock;
    }

    private record MenuTemplate(
            String sabjiOne,
            String sabjiTwo,
            String dal,
            String rice,
            String sweet
    ) {
    }

    /*
     * Seeds sample menus within the dedicated demo mess.
     *
     * SeedDataService selects and passes the demo mess.
     * Other messes' menus do not affect this seed check.
     *
     * Dish records are created/reused for this mess and
     * then referenced by the seeded menus.
     */
    @Transactional
    public void seed(Mess mess) {

        if (mess == null || mess.getMessId() == null) {

            throw new IllegalArgumentException(
                    "A persisted demo mess is required."
            );
        }

        if (!menuRepository
                .findAllByMess_MessIdOrderByMenuDateAscMealSessionAsc(
                        mess.getMessId()
                )
                .isEmpty()) {

            log.info(
                    "Menus already exist for demo mess {}. Skipping demo seeding.",
                    mess.getMessId()
            );

            return;
        }

        List<MenuTemplate> templates =
                buildMenuTemplates();

        Random random =
                new Random(
                        RANDOM_SEED
                );

        LocalDate today =
                LocalDate.now(clock);

        LocalDate historicalStartDate =
                today.minusDays(
                        HISTORICAL_DAY_COUNT
                );

        LocalDate historicalEndDate =
                today.minusDays(1);

        int menuCount = 0;

        /*
         * Seed lunch and dinner menus for the
         * historical period.
         */
        for (LocalDate date = historicalStartDate;
             !date.isAfter(historicalEndDate);
             date = date.plusDays(1)) {

            MenuTemplate lunchTemplate =
                    getRandomTemplate(
                            templates,
                            random
                    );

            MenuTemplate dinnerTemplate =
                    getDifferentTemplate(
                            templates,
                            lunchTemplate,
                            random
                    );

            saveMenu(
                    mess,
                    date,
                    MealSession.LUNCH,
                    lunchTemplate
            );

            saveMenu(
                    mess,
                    date,
                    MealSession.DINNER,
                    dinnerTemplate
            );

            menuCount += 2;
        }

        /*
         * Seed only today's lunch menu.
         *
         * Today's dinner remains unpublished so that
         * the owner can test the menu-publishing flow.
         */
        MenuTemplate todayLunchTemplate =
                getRandomTemplate(
                        templates,
                        random
                );

        saveMenu(
                mess,
                today,
                MealSession.LUNCH,
                todayLunchTemplate
        );

        menuCount++;

        log.info(
                "Demo Menus seeded successfully for mess {}. "
                        + "Historical days: {}, Total menus: {}.",
                mess.getMessId(),
                HISTORICAL_DAY_COUNT,
                menuCount
        );
    }

    private MenuTemplate getRandomTemplate(
            List<MenuTemplate> templates,
            Random random) {

        return templates.get(
                random.nextInt(
                        templates.size()
                )
        );
    }

    private MenuTemplate getDifferentTemplate(
            List<MenuTemplate> templates,
            MenuTemplate existingTemplate,
            Random random) {

        MenuTemplate template =
                getRandomTemplate(
                        templates,
                        random
                );

        while (template.equals(existingTemplate)) {

            template =
                    getRandomTemplate(
                            templates,
                            random
                    );
        }

        return template;
    }

    private void saveMenu(
            Mess mess,
            LocalDate date,
            MealSession mealSession,
            MenuTemplate template) {

        Menu menu =
                new Menu();

        menu.setMess(mess);

        menu.setMenuDate(
                date
        );

        menu.setMealSession(
                mealSession
        );

        menu.setSabjiOne(
                findOrCreateDish(
                        mess,
                        template.sabjiOne(),
                        DishCategory.SABJI
                )
        );

        menu.setSabjiTwo(
                findOrCreateDish(
                        mess,
                        template.sabjiTwo(),
                        DishCategory.SABJI
                )
        );

        menu.setDal(
                findOrCreateDish(
                        mess,
                        template.dal(),
                        DishCategory.DAL
                )
        );

        menu.setRice(
                findOrCreateDish(
                        mess,
                        template.rice(),
                        DishCategory.RICE
                )
        );

        menu.setSweet(
                findOrCreateDish(
                        mess,
                        template.sweet(),
                        DishCategory.SWEET
                )
        );

        menuRepository.save(
                menu
        );
    }

    private Dish findOrCreateDish(
            Mess mess,
            String name,
            DishCategory category) {

        String normalizedName =
                dishNameNormalizer.normalize(
                        name
                );

        return dishRepository
                .findByMess_MessIdAndNormalizedName(
                        mess.getMessId(),
                        normalizedName
                )
                .map(existingDish -> {

                    if (existingDish.getCategory() != category) {

                        throw new IllegalStateException(
                                "Seed dish already exists under a different category: "
                                        + name
                        );
                    }

                    return existingDish;
                })
                .orElseGet(() -> {

                    Dish dish =
                            new Dish();

                    dish.setMess(mess);

                    dish.setName(
                            name.trim().replaceAll(
                                    "\\s+",
                                    " "
                            )
                    );

                    dish.setNormalizedName(
                            normalizedName
                    );

                    dish.setCategory(
                            category
                    );

                    dish.setActive(true);

                    return dishRepository.save(
                            dish
                    );
                });
    }

    private List<MenuTemplate> buildMenuTemplates() {

        return List.of(

                new MenuTemplate(
                        "Aloo Matar",
                        "Bhindi Fry",
                        "Dal Tadka",
                        "Jeera Rice",
                        "Gulab Jamun"
                ),

                new MenuTemplate(
                        "Mix Veg",
                        "Paneer Bhurji",
                        "Dal Fry",
                        "Steamed Rice",
                        "Kheer"
                ),

                new MenuTemplate(
                        "Aloo Gobi",
                        "Cabbage Peas",
                        "Moong Dal",
                        "Jeera Rice",
                        "Shrikhand"
                ),

                new MenuTemplate(
                        "Baingan Masala",
                        "Aloo Beans",
                        "Dal Tadka",
                        "Plain Rice",
                        "Jalebi"
                ),

                new MenuTemplate(
                        "Matar Paneer",
                        "Bhindi Masala",
                        "Dal Fry",
                        "Jeera Rice",
                        "Basundi"
                ),

                new MenuTemplate(
                        "Kobi Batata",
                        "Chana Masala",
                        "Masoor Dal",
                        "Steamed Rice",
                        "Gulab Jamun"
                ),

                new MenuTemplate(
                        "Palak Paneer",
                        "Aloo Jeera",
                        "Dal Tadka",
                        "Jeera Rice",
                        "Kheer"
                ),

                new MenuTemplate(
                        "Veg Kolhapuri",
                        "Patta Gobi",
                        "Dal Fry",
                        "Plain Rice",
                        "Rasgulla"
                ),

                new MenuTemplate(
                        "Bhindi Masala",
                        "Aloo Matar",
                        "Toor Dal",
                        "Jeera Rice",
                        "Sheera"
                ),

                new MenuTemplate(
                        "Paneer Masala",
                        "Mix Veg Fry",
                        "Moong Dal",
                        "Steamed Rice",
                        "Puran Poli"
                )
        );
    }
}