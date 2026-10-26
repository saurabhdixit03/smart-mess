package com.smartmess.backend.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartmess.backend.entity.Dish;
import com.smartmess.backend.entity.Mess;
import com.smartmess.backend.enums.DishCategory;
import com.smartmess.backend.repository.DishRepository;
import com.smartmess.backend.repository.MessRepository;
import com.smartmess.backend.service.DishNameNormalizer;
import com.smartmess.backend.service.DishService;

@Service
public class DishServiceImpl implements DishService {

    private final DishRepository dishRepository;
    private final MessRepository messRepository;
    private final DishNameNormalizer dishNameNormalizer;

    public DishServiceImpl(
            DishRepository dishRepository,
            MessRepository messRepository,
            DishNameNormalizer dishNameNormalizer
    ) {
        this.dishRepository = dishRepository;
        this.messRepository = messRepository;
        this.dishNameNormalizer = dishNameNormalizer;
    }

    @Override
    @Transactional
    public Dish findOrCreateDish(
            Long messId,
            String name,
            DishCategory category
    ) {

        String normalizedName = dishNameNormalizer.normalize(name);

        if (normalizedName == null || normalizedName.isBlank()) {
            throw new IllegalArgumentException(
                    "Dish name must not be blank"
            );
        }

        if (category == null) {
            throw new IllegalArgumentException(
                    "Dish category is required"
            );
        }

        return dishRepository
                .findByMess_MessIdAndNormalizedName(
                        messId,
                        normalizedName
                )
                .map(existingDish -> {

                    if (existingDish.getCategory() != category) {
                        throw new IllegalArgumentException(
                                "Dish already exists under a different category"
                        );
                    }

                    return existingDish;
                })
                .orElseGet(() -> {

                    Mess mess = messRepository.findById(messId)
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Mess not found"
                                    )
                            );

                    Dish dish = new Dish();
                    dish.setMess(mess);
                    dish.setName(name.trim().replaceAll("\\s+", " "));
                    dish.setNormalizedName(normalizedName);
                    dish.setCategory(category);
                    dish.setActive(true);

                    return dishRepository.save(dish);
                });
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dish> searchActiveDishes(
            Long messId,
            DishCategory category,
            String query
    ) {

        String normalizedQuery =
                dishNameNormalizer.normalize(query);

        if (normalizedQuery == null || normalizedQuery.isBlank()) {
            return dishRepository
                    .findByMess_MessIdAndCategoryAndActiveTrueOrderByNameAsc(
                            messId,
                            category
                    );
        }

        return dishRepository
                .findTop10ByMess_MessIdAndCategoryAndActiveTrueAndNormalizedNameContainingOrderByNameAsc(
                        messId,
                        category,
                        normalizedQuery
                );
    }
}