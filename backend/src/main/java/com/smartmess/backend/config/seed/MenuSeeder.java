package com.smartmess.backend.config.seed;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.smartmess.backend.entity.Menu;
import com.smartmess.backend.enums.MealSession;
import com.smartmess.backend.repository.MenuRepository;

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

    private final Clock clock;

    public MenuSeeder(
            MenuRepository menuRepository,
            Clock clock) {

        this.menuRepository =
                menuRepository;

        this.clock =
                clock;
    }

    private record MenuTemplate(
            String sabjiOne,
            String sabjiTwo,
            String dal,
            String rice,
            String sweet
    ) {
    }

    public void seed() {

        if (menuRepository.count() > 0) {

            log.info(
                    "Menus already exist. Skipping demo seeding."
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
                    date,
                    MealSession.LUNCH,
                    lunchTemplate
            );

            saveMenu(
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
                today,
                MealSession.LUNCH,
                todayLunchTemplate
        );

        menuCount++;

        log.info(
                "Demo Menus seeded successfully. "
                        + "Historical days: {}, Total menus: {}.",
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
            LocalDate date,
            MealSession mealSession,
            MenuTemplate template) {

        Menu menu =
                new Menu();

        menu.setMenuDate(
                date
        );

        menu.setMealSession(
                mealSession
        );

        menu.setSabjiOne(
                template.sabjiOne()
        );

        menu.setSabjiTwo(
                template.sabjiTwo()
        );

        menu.setDal(
                template.dal()
        );

        menu.setRice(
                template.rice()
        );

        menu.setSweet(
                template.sweet()
        );

        menuRepository.save(
                menu
        );
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