package com.smartmess.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmess.backend.entity.MessSettings;

public interface MessSettingsRepository
        extends JpaRepository<MessSettings, Long> {

    /*
     * Tenant-scoped settings lookup.
     *
     * Each mess has at most one settings record.
     */
    Optional<MessSettings> findByMess_MessId(Long messId);

    /*
     * Checks whether settings already exist for a mess.
     */
    boolean existsByMess_MessId(Long messId);
}