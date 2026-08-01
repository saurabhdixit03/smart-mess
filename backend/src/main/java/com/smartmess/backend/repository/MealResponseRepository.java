package com.smartmess.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MealResponse;
import com.smartmess.backend.entity.Menu;
import com.smartmess.backend.enums.MealOption;
import com.smartmess.backend.enums.MealResponseStatus;

public interface MealResponseRepository extends JpaRepository<MealResponse, Long> {

    Optional<MealResponse> findByCustomerAndMenu(Customer customer, Menu menu);
    
    Optional<MealResponse> findByCustomerCustomerIdAndMenuMenuId(
            Long customerId,
            Long menuId);

    List<MealResponse> findByMenu(Menu menu);

    List<MealResponse> findByCustomer(Customer customer);
    
    // Live Dashboard 
    
    long countByMenuAndResponseStatus(
            Menu menu,
            MealResponseStatus responseStatus);

    long countByMenuAndMealOption(
            Menu menu,
            MealOption mealOption);

    @Query("""
           SELECT COALESCE(SUM(m.extraRotiCount),0)
    		FROM MealResponse m
    		WHERE m.menu = :menu
    		AND m.responseStatus = com.smartmess.backend.enums.MealResponseStatus.ACCEPTED
           """)
    Long getTotalExtraRotis(Menu menu);
    
    // collection queue for meal record module  
    
    @Query("""
    	    SELECT mr
    	    FROM MealResponse mr
    	    WHERE mr.menu = :menu
    	      AND mr.responseStatus = com.smartmess.backend.enums.MealResponseStatus.ACCEPTED
    	      AND NOT EXISTS (
    	            SELECT 1
    	            FROM MealRecord rec
    	            WHERE rec.mealResponse = mr
    	      )
    	    ORDER BY mr.customer.fullName
    	    """)
    	List<MealResponse> findCollectionQueue(Menu menu);

}