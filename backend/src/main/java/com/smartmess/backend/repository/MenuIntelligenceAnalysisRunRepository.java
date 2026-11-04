package com.smartmess.backend.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.smartmess.backend.entity.MenuIntelligenceAnalysisRun;
import com.smartmess.backend.enums.AnalysisRunStatus;

import jakarta.persistence.LockModeType;

public interface MenuIntelligenceAnalysisRunRepository
        extends JpaRepository<MenuIntelligenceAnalysisRun, Long> {

    Optional<MenuIntelligenceAnalysisRun>
    findFirstByMess_MessIdAndStatusOrderByAnalysisRunIdDesc(
            Long messId,
            AnalysisRunStatus status
    );

    boolean existsByMess_MessIdAndPeriodStartAndPeriodEndAndStatus(
            Long messId,
            LocalDate periodStart,
            LocalDate periodEnd,
            AnalysisRunStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT run
            FROM MenuIntelligenceAnalysisRun run
            WHERE run.mess.messId = :messId
              AND run.periodStart = :periodStart
              AND run.periodEnd = :periodEnd
            """)
    Optional<MenuIntelligenceAnalysisRun> findByMessAndPeriodForUpdate(
            @Param("messId") Long messId,
            @Param("periodStart") LocalDate periodStart,
            @Param("periodEnd") LocalDate periodEnd
    );
}