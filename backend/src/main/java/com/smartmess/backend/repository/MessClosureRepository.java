package com.smartmess.backend.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmess.backend.entity.MessClosure;

public interface MessClosureRepository
        extends JpaRepository<MessClosure, Long> {

    List<MessClosure> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate startDate,
            LocalDate endDate
    );

    List<MessClosure> findByEndDateGreaterThanEqualOrderByStartDateAsc(
            LocalDate date
    );

    List<MessClosure> findAllByOrderByStartDateDesc();
}