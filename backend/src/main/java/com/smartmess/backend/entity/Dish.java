package com.smartmess.backend.entity;

import com.smartmess.backend.common.BaseEntity;
import com.smartmess.backend.enums.DishCategory;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "dishes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_dishes_mess_normalized_name",
                        columnNames = {
                                "mess_id",
                                "normalized_name"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_dishes_mess_category",
                        columnList = "mess_id, category"
                )
        }
)
public class Dish extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dish_id")
    private Long dishId;

    /*
     * Tenant Ownership
     *
     * Each dish belongs to exactly one mess.
     * Different messes may have dishes with the same name.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mess_id", nullable = false)
    private Mess mess;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /*
     * Used for tenant-scoped duplicate detection and lookup.
     * Example:
     * "  Bhindi   Masala " -> "bhindi masala"
     */
    @Column(name = "normalized_name", nullable = false, length = 100)
    private String normalizedName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private DishCategory category;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}