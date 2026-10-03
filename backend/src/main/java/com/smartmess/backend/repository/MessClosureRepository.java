package com.smartmess.backend.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.smartmess.backend.entity.MessClosure;

public interface MessClosureRepository
        extends JpaRepository<MessClosure, Long> {

    /*
     * Finds a closure only within the specified mess.
     */
    Optional<MessClosure> findByClosureIdAndMess_MessId(
            Long closureId,
            Long messId
    );

    /*
     * Finds date-overlap candidates within one mess.
     *
     * Pass the requested end date as startDate,
     * and the requested start date as endDate.
     * Meal-session boundaries are checked by the service.
     */
    List<MessClosure>
            findByMess_MessIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
                    Long messId,
                    LocalDate startDate,
                    LocalDate endDate
            );

    /*
     * Returns current and upcoming closures within one mess.
     */
    List<MessClosure>
            findByMess_MessIdAndEndDateGreaterThanEqualOrderByStartDateAsc(
                    Long messId,
                    LocalDate date
            );

    /*
     * Preserves the existing history behaviour:
     * all closures, ordered by start date descending,
     * restricted to one mess.
     */
    List<MessClosure> findAllByMess_MessIdOrderByStartDateDesc(
            Long messId
    );
}