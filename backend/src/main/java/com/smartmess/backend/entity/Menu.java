package com.smartmess.backend.entity;

import java.time.LocalDate;

import com.smartmess.backend.common.BaseEntity;
import com.smartmess.backend.enums.MealSession;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "menus",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {
                "menu_date",
                "meal_session"
            }
        )
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Menu extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long menuId;

    @Column(name = "menu_date", nullable = false)
    private LocalDate menuDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_session", nullable = false)
    private MealSession mealSession;

    @Column(name = "sabji_one", nullable = false, length = 100)
    private String sabjiOne;

    @Column(name = "sabji_two", length = 100)
    private String sabjiTwo;

    @Column(length = 100)
    private String dal;

    @Column(length = 100)
    private String rice;

    @Column(length = 100)
    private String sweet;

}