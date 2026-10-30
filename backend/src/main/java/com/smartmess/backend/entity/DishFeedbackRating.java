package com.smartmess.backend.entity;

import com.smartmess.backend.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
        name = "dish_feedback_ratings",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_dish_feedback_rating_feedback_dish",
                        columnNames = {
                                "meal_feedback_id",
                                "dish_id"
                        }
                )
        }
)
public class DishFeedbackRating extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dish_feedback_rating_id")
    private Long dishFeedbackRatingId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_feedback_id", nullable = false)
    private MealFeedback mealFeedback;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "dish_id", nullable = false)
    private Dish dish;

    @Column(name = "rating", nullable = false)
    private Integer rating;
}