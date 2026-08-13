
package com.smartmess.backend.config.seed;

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
            LoggerFactory.getLogger(MenuSeeder.class);

    private final MenuRepository menuRepository;

    private final Random random = new Random();

    public MenuSeeder(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
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
            return;
        }

        List<MenuTemplate> templates = buildMenuTemplates();

        LocalDate startDate = LocalDate.now().minusDays(43);
        LocalDate endDate = LocalDate.now();

        for (LocalDate date = startDate;
             !date.isAfter(endDate);
             date = date.plusDays(1)) {

            MenuTemplate lunchTemplate =
                    templates.get(random.nextInt(templates.size()));

            MenuTemplate dinnerTemplate =
                    templates.get(random.nextInt(templates.size()));

            // Avoid identical lunch and dinner menus on the same day.
            while (dinnerTemplate == lunchTemplate) {
                dinnerTemplate =
                        templates.get(random.nextInt(templates.size()));
            }

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
        }

        log.info(
                "Demo Menus seeded successfully for {} days ({} menus).",
                44,
                88
        );
    }

    private void saveMenu(
            LocalDate date,
            MealSession mealSession,
            MenuTemplate template) {

        Menu menu = new Menu();

        menu.setMenuDate(date);
        menu.setMealSession(mealSession);

        menu.setSabjiOne(template.sabjiOne());
        menu.setSabjiTwo(template.sabjiTwo());
        menu.setDal(template.dal());
        menu.setRice(template.rice());
        menu.setSweet(template.sweet());

        menuRepository.save(menu);
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

