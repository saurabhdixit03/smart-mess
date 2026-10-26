package com.smartmess.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartmess.backend.entity.Dish;
import com.smartmess.backend.enums.DishCategory;

@Repository
public interface DishRepository extends JpaRepository<Dish, Long> {

    Optional<Dish> findByDishIdAndMess_MessId(
            Long dishId,
            Long messId
    );

    Optional<Dish> findByMess_MessIdAndNormalizedName(
            Long messId,
            String normalizedName
    );

    List<Dish> findByMess_MessIdAndCategoryAndActiveTrueOrderByNameAsc(
            Long messId,
            DishCategory category
    );
    
    List<Dish> findTop10ByMess_MessIdAndCategoryAndActiveTrueAndNormalizedNameContainingOrderByNameAsc(
            Long messId,
            DishCategory category,
            String normalizedName
    );
}