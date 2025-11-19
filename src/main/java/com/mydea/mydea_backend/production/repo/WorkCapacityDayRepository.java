package com.mydea.mydea_backend.production.repo;

import com.mydea.mydea_backend.production.domain.WorkCapacityDay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkCapacityDayRepository extends JpaRepository<WorkCapacityDay, LocalDate> {

    Optional<WorkCapacityDay> findByWorkDate(LocalDate date);

    @Query("select d from WorkCapacityDay d where d.workDate = :date and (d.capacityMin - d.reservedMin) >= :required")
    Optional<WorkCapacityDay> findAvailableOn(@Param("date") LocalDate date, @Param("required") int requiredMinutes);

    @Query("select d from WorkCapacityDay d where d.workDate between :start and :end and (d.capacityMin - d.reservedMin) >= :required order by d.workDate asc")
    List<WorkCapacityDay> findAvailableBetween(@Param("start") LocalDate start,
            @Param("end") LocalDate end,
            @Param("required") int requiredMinutes);

    @Query("select count(d) from WorkCapacityDay d where d.workDate between :start and :end")
    long countInRange(@Param("start") LocalDate start, @Param("end") LocalDate end);
}
