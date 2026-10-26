package com.smartmess.backend.service;

import java.util.List;

import com.smartmess.backend.entity.Dish;
import com.smartmess.backend.enums.DishCategory;

public interface DishService {

    Dish findOrCreateDish(
            Long messId,
            String name,
            DishCategory category
    );

    List<Dish> searchActiveDishes(
            Long messId,
            DishCategory category,
            String query
    );
}