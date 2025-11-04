package com.mydea.mydea_backend.production.repo;

import com.mydea.mydea_backend.production.domain.WorkSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WorkSlotRepository extends JpaRepository<WorkSlot, Long> {
    Optional<WorkSlot> findByWorkDateAndSlotIndexAndResource(LocalDate workDate, Integer slotIndex, String resource);

    List<WorkSlot> findByWorkDateAndResourceOrderBySlotIndexAsc(LocalDate workDate, String resource);

    @Query("select s from WorkSlot s where s.workDate = :date and s.resource = :resource and (s.capacityMin - s.reservedMin) >= :required order by s.slotIndex asc")
    List<WorkSlot> findAvailableSlots(@Param("date") LocalDate workDate,
            @Param("resource") String resource,
            @Param("required") int requiredMinutes);
}
