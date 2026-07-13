package com.smartmess.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmess.backend.entity.MessSettings;

public interface MessSettingsRepository
        extends JpaRepository<MessSettings, Long> {

    Optional<MessSettings> findTopByOrderBySettingsIdAsc();

}