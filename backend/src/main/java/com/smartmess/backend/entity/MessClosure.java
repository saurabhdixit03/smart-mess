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
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(
        name = "mess_closures",
        indexes = {
                @Index(
                        name = "idx_mess_closures_mess_start",
                        columnList = "mess_id, start_date"
                )
        }
)
public class MessClosure extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "closure_id")
    private Long closureId;

    /*
     * Tenant Ownership
     *
     * A temporary closure applies only to its owning mess.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mess_id", nullable = false)
    private Mess mess;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "start_session", nullable = false, length = 20)
    private MealSession startSession;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "end_session", nullable = false, length = 20)
    private MealSession endSession;

    @Column(name = "reason", nullable = false, length = 255)
    private String reason;
}