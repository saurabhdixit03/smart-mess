package com.smartmess.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmess.backend.entity.Customer;
import com.smartmess.backend.entity.Notification;

public interface NotificationRepository
        extends JpaRepository<Notification, Long> {

    List<Notification>
            findByCustomerOrderByCreatedAtDesc(
                    Customer customer
            );

    long countByCustomerAndReadFalse(
            Customer customer
    );

    List<Notification>
            findByCustomerAndReadFalseOrderByCreatedAtDesc(
                    Customer customer
            );
}