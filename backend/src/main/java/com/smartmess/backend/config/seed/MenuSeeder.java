package com.smartmess.backend.config.seed;

import java.time.LocalDate;
import java.util.List;

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

    public MenuSeeder(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    private record MenuSeed(

            LocalDate menuDate,

            MealSession mealSession,

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

        for (MenuSeed seed : buildDemoMenus()) {

            Menu menu = new Menu();

            menu.setMenuDate(seed.menuDate());

            menu.setMealSession(seed.mealSession());

            menu.setSabjiOne(seed.sabjiOne());

            menu.setSabjiTwo(seed.sabjiTwo());

            menu.setDal(seed.dal());

            menu.setRice(seed.rice());

            menu.setSweet(seed.sweet());

            menuRepository.save(menu);
        }

        log.info("Demo Menus seeded successfully.");
    }

    private List<MenuSeed> buildDemoMenus() {

        return List.of(

                new MenuSeed(
                        LocalDate.now(),
                        MealSession.LUNCH,
                        "Aloo Matar",
                        "Bhindi Fry",
                        "Dal Tadka",
                        "Jeera Rice",
                        "Gulab Jamun"
                ),

                new MenuSeed(
                        LocalDate.now(),
                        MealSession.DINNER,
                        "Mix Veg",
                        "Paneer Bhurji",
                        "Dal Fry",
                        "Steamed Rice",
                        "Kheer"
                )

        );
    }
}