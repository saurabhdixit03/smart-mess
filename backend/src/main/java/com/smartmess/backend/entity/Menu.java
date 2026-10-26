package com.smartmess.backend.entity;

import java.time.LocalDate;

import com.smartmess.backend.common.BaseEntity;
import com.smartmess.backend.enums.MealSession;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
                        name = "uk_menus_mess_date_session",
                        columnNames = {
                                "mess_id",
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

    /*
     * Tenant Ownership
     *
     * Each menu belongs to one mess.
     * Different messes can publish menus for the same date
     * and meal session.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mess_id", nullable = false)
    private Mess mess;

    @Column(name = "menu_date", nullable = false)
    private LocalDate menuDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "meal_session", nullable = false)
    private MealSession mealSession;

    /*
     * Dish References
     *
     * Menu no longer stores dish names directly.
     * Each menu item now points to a tenant-scoped Dish.
     *
     * sabjiOne is required.
     * The remaining menu items are optional.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sabji_one_dish_id", nullable = false)
    private Dish sabjiOne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sabji_two_dish_id")
    private Dish sabjiTwo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dal_dish_id")
    private Dish dal;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rice_dish_id")
    private Dish rice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sweet_dish_id")
    private Dish sweet;
}