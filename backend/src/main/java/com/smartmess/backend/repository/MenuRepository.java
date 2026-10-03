package com.smartmess.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmess.backend.entity.Menu;
import com.smartmess.backend.enums.MealSession;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    /*
     * Tenant-scoped published-menu check.
     *
     * Used by menu publishing and closure boundary resolution.
     */
    boolean existsByMess_MessIdAndMenuDateAndMealSession(
            Long messId,
            LocalDate menuDate,
            MealSession mealSession
    );

    /*
     * Finds a specific meal session's menu within one mess.
     */
    Optional<Menu> findByMess_MessIdAndMenuDateAndMealSession(
            Long messId,
            LocalDate menuDate,
            MealSession mealSession
    );

    /*
     * Finds a menu by ID only within its owning mess.
     */
    Optional<Menu> findByMenuIdAndMess_MessId(
            Long menuId,
            Long messId
    );

    /*
     * Returns menus for a date within one mess,
     * preserving the existing session ordering.
     */
    List<Menu> findByMess_MessIdAndMenuDateOrderByMealSessionAsc(
            Long messId,
            LocalDate menuDate
    );

    /*
     * Returns menu history within one mess.
     */
    List<Menu> findAllByMess_MessIdOrderByMenuDateDescMealSessionAsc(
            Long messId
    );

    /*
     * Returns menus chronologically within one mess.
     */
    List<Menu> findAllByMess_MessIdOrderByMenuDateAscMealSessionAsc(
            Long messId
    );
}