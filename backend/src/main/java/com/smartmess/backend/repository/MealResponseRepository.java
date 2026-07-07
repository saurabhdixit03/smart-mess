package com.smartmess.backend.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.MealResponse;
import com.smartmess.backend.entity.Menu;

public interface MealResponseRepository extends JpaRepository<MealResponse, Long> {

    Optional<MealResponse> findByCustomerAndMenu(Customer customer, Menu menu);

    List<MealResponse> findByMenu(Menu menu);

    List<MealResponse> findByCustomer(Customer customer);

}