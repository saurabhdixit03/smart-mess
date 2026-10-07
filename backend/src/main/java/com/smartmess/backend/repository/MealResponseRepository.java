package com.smartmess.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MealResponse;
import com.smartmess.backend.entity.Menu;
import com.smartmess.backend.enums.MealOption;
import com.smartmess.backend.enums.MealResponseStatus;

public interface MealResponseRepository
        extends JpaRepository<MealResponse, Long> {

    /*
     * Tenant-scoped response lookups.
     */
    Optional<MealResponse> findByMess_MessIdAndCustomerAndMenu(
            Long messId,
            Customer customer,
            Menu menu
    );

    Optional<MealResponse> findByMealResponseIdAndMess_MessId(
            Long mealResponseId,
            Long messId
    );

    List<MealResponse> findByMess_MessIdAndMenu(
            Long messId,
            Menu menu
    );

    List<MealResponse> findByMess_MessIdAndCustomer(
            Long messId,
            Customer customer
    );

    /*
     * Responses for active customer search results.
     * Both accepted and declined responses are included.
     *
     * Call only when customerIds is non-empty.
     */
    @Query("""
            SELECT response
            FROM MealResponse response
            JOIN FETCH response.customer customer
            WHERE response.mess.messId = :messId
              AND customer.mess.messId = :messId
              AND response.menu.mess.messId = :messId
              AND response.menu = :menu
              AND customer.customerId IN :customerIds
            """)
    List<MealResponse> findForCollectionSearch(
            @Param("messId") Long messId,
            @Param("menu") Menu menu,
            @Param("customerIds") List<Long> customerIds
    );

    /*
     * Live dashboard response counts.
     */
    long countByMess_MessIdAndMenuAndResponseStatus(
            Long messId,
            Menu menu,
            MealResponseStatus responseStatus
    );

    long countByMess_MessIdAndMenuAndMealOption(
            Long messId,
            Menu menu,
            MealOption mealOption
    );

    @Query("""
            SELECT COALESCE(SUM(m.extraRotiCount), 0)
            FROM MealResponse m
            WHERE m.mess.messId = :messId
              AND m.menu = :menu
              AND m.responseStatus =
                  com.smartmess.backend.enums.MealResponseStatus.ACCEPTED
            """)
    Long getTotalExtraRotisByMess(
            @Param("messId") Long messId,
            @Param("menu") Menu menu
    );

    /*
     * Collection queue.
     *
     * Includes only active customers who accepted this menu
     * and have not collected their meal.
     *
     * Customer/menu matching also detects collection without
     * a linked meal response.
     */
    @Query("""
            SELECT mr
            FROM MealResponse mr
            WHERE mr.mess.messId = :messId
              AND mr.customer.mess.messId = :messId
              AND mr.menu.mess.messId = :messId
              AND mr.menu = :menu
              AND mr.customer.status =
                  com.smartmess.backend.enums.CustomerStatus.ACTIVE
              AND mr.responseStatus =
                  com.smartmess.backend.enums.MealResponseStatus.ACCEPTED
              AND NOT EXISTS (
                    SELECT rec.mealRecordId
                    FROM MealRecord rec
                    WHERE rec.mess.messId = :messId
                      AND rec.customer = mr.customer
                      AND rec.menu = mr.menu
              )
            ORDER BY mr.customer.fullName, mr.customer.customerId
            """)
    List<MealResponse> findCollectionQueueByMess(
            @Param("messId") Long messId,
            @Param("menu") Menu menu
    );
}