package com.smartmess.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmess.backend.entity.Menu;
import com.smartmess.backend.enums.MealSession;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    boolean existsByMenuDateAndMealSession(LocalDate menuDate,
                                          MealSession mealSession);

    Optional<Menu> findByMenuDateAndMealSession(LocalDate menuDate,
                                                MealSession mealSession);

    List<Menu> findByMenuDateOrderByMealSessionAsc(LocalDate menuDate);

    List<Menu> findAllByOrderByMenuDateDescMealSessionAsc();

}