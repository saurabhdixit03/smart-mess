package com.smartmess.backend.entity;

import com.smartmess.backend.common.BaseEntity;
import com.smartmess.backend.enums.MessOwnerStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "mess_owners")
public class MessOwner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mess_owner_id")
    private Long messOwnerId;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mess_id", nullable = false, unique = true)
    private Mess mess;

    @Column(name = "mobile_number", nullable = false, unique = true, length = 10)
    private String mobileNumber;

    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private MessOwnerStatus status;

    @PrePersist
    private void initializeDefaults() {

        if (status == null) {
            status = MessOwnerStatus.ACTIVE;
        }
    }
}