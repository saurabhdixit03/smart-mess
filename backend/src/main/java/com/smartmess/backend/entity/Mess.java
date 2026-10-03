package com.smartmess.backend.entity;

import com.smartmess.backend.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "messes")
public class Mess extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mess_id")
    private Long messId;

    @Column(name = "mess_name", nullable = false, length = 100)
    private String messName;

    @Column(
            name = "registration_code",
            nullable = false,
            unique = true,
            length = 36
    )
    private String registrationCode;
}