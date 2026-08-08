package com.smartmess.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.smartmess.backend.entity.MessOwner;
import com.smartmess.backend.enums.MessOwnerStatus;

@Repository
public interface MessOwnerRepository extends JpaRepository<MessOwner, Long> {

    boolean existsByMobileNumber(String mobileNumber);

    boolean existsByEmail(String email);

    Optional<MessOwner> findByMobileNumber(String mobileNumber);

    Optional<MessOwner> findByEmail(String email);

    Optional<MessOwner> findByMessOwnerIdAndStatus(
            Long messOwnerId,
            MessOwnerStatus status
    );
}