package com.mydea.mydea_backend.production.service;

import com.mydea.mydea_backend.production.domain.WorkCapacityDay;
import com.mydea.mydea_backend.production.domain.WorkerInfo;
import com.mydea.mydea_backend.production.repo.WorkCapacityDayRepository;
import com.mydea.mydea_backend.production.repo.WorkerInfoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * 매주 수요일 새벽 1시에 다음 주(월~일)의 일일 생산 용량(work_capacity_day)을 준비.
 * 이미 모든 날짜 레코드가 존재하면 스킵.
 */
@Service
@RequiredArgsConstructor
public class CapacityBatchService {
    private final WorkerInfoRepository workerRepo;
    private final WorkCapacityDayRepository capacityRepo;

    // 매주 수요일 새벽 1시 (초 분 시 일 월 요일)
    @Scheduled(cron = "0 0 1 * * WED")
    @Transactional
    public void prepareNextWeekCapacity() {
        createNextWeekCapacityIfNotExists();
    }

    @Transactional
    public void createNextWeekCapacityIfNotExists() {
        LocalDate today = LocalDate.now();
        LocalDate nextWeekStart = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalDate nextWeekEnd = nextWeekStart.plusDays(6); // 월~일 7일

        long existingCount = capacityRepo.countInRange(nextWeekStart, nextWeekEnd);
        long expected = 7L; // 월~일 7일
        if (existingCount == expected) {
            return; // 이미 모두 존재
        }

        List<WorkerInfo> workers = workerRepo.findByActiveTrue();

        for (LocalDate d = nextWeekStart; !d.isAfter(nextWeekEnd); d = d.plusDays(1)) {
            if (capacityRepo.existsById(d))
                continue; // 개별 존재시 skip
            int dow = d.getDayOfWeek().getValue(); // 1=MON ... 7=SUN
            int capacityMin = sumDailyCapacity(workers, dow);
            WorkCapacityDay day = WorkCapacityDay.builder()
                    .workDate(d)
                    .capacityMin(capacityMin)
                    .reservedMin(0)
                    .backlogAcceptedCount(0)
                    .version(0)
                    .build();
            capacityRepo.save(day);
        }
    }

    private int sumDailyCapacity(List<WorkerInfo> workers, int dow1to7) {
        DayOfWeek dow = DayOfWeek.of(dow1to7);
        return workers.stream()
                .mapToInt(w -> w.minutesFor(dow))
                .sum();
    }
}
