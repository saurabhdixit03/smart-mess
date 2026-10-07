package com.smartmess.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmess.backend.entity.MealPricing;

public interface MealPricingRepository
        extends JpaRepository<MealPricing, Long> {

    /*
     * Current pricing at a supplied application-clock time.
     * Scheduled future versions are excluded.
     */
    Optional<MealPricing>
    findTopByMess_MessIdAndEffectiveFromLessThanEqualOrderByEffectiveFromDesc(
            Long messId,
            LocalDateTime effectiveAt
    );

    /*
     * Future scheduled pricing, earliest first.
     */
    List<MealPricing>
    findByMess_MessIdAndEffectiveFromGreaterThanOrderByEffectiveFromAsc(
            Long messId,
            LocalDateTime effectiveAt
    );

    /*
     * Complete pricing history for one mess.
     */
    List<MealPricing> findByMess_MessIdOrderByEffectiveFromDesc(
            Long messId
    );

    /*
     * Resolve a specific version within its tenant.
     */
    Optional<MealPricing> findByMealPricingIdAndMess_MessId(
            Long mealPricingId,
            Long messId
    );

    /*
     * Detect an existing version at the requested effective time.
     */
    Optional<MealPricing> findByMess_MessIdAndEffectiveFrom(
            Long messId,
            LocalDateTime effectiveFrom
    );

    /*
     * Initialization checks for any existing pricing version.
     */
    boolean existsByMess_MessId(Long messId);
}