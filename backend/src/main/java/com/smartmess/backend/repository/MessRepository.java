package com.smartmess.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmess.backend.entity.Mess;

public interface MessRepository extends JpaRepository<Mess, Long> {

    Optional<Mess> findByRegistrationCode(String registrationCode);
}