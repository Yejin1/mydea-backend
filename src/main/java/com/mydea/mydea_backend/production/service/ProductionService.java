package com.mydea.mydea_backend.production.service;

import com.mydea.mydea_backend.production.domain.ProductionProductType;
import com.mydea.mydea_backend.production.domain.WorkSlot;
import com.mydea.mydea_backend.production.domain.WorkTimeRangeRule;
import com.mydea.mydea_backend.production.repo.WorkSlotRepository;
import com.mydea.mydea_backend.production.repo.WorkTimeRangeRuleRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ProductionService {
    private final WorkTimeRangeRuleRepository timeRules;
    private final WorkSlotRepository slots;

    public record ReservedSlot(Long slotId, LocalDate date, int slotIndex, String resource, int reservedMinutes) {
    }

    @Transactional(readOnly = true)
    public int estimateMinutes(ProductionProductType type, BigDecimal size, int quantity) {
        WorkTimeRangeRule rule = timeRules
                .findFirstByProductTypeAndSizeMinLessThanEqualAndSizeMaxGreaterThanEqualOrderByPriorityAsc(type, size,
                        size)
                .orElseThrow(() -> new NoSuchElementException("해당 규칙을 찾을 수 없습니다: type=" + type + ", size=" + size));
        if (quantity <= 0)
            throw new IllegalArgumentException("quantity(수량)은 양수여야 합니다");
        return rule.getMinPerUnit() * quantity;
    }

    /**
     * 지정한 날짜와 자원(resource)에 대해, 필요한 분(requiredMinutes)을
     * 가장 이른 단일 슬롯에 예약
     * 슬롯 행에 낙관적 락을 사용하며, 경합이 발생하면 3번 재시도
     */
    @Transactional
    public ReservedSlot reserveInFirstAvailableSlot(LocalDate date, String resource, int requiredMinutes) {
        if (requiredMinutes <= 0)
            throw new IllegalArgumentException("requiredMinutes(필요 분)은 양수여야 합니다");
        List<WorkSlot> candidates = slots.findAvailableSlots(date, resource, requiredMinutes);
        if (candidates.isEmpty()) {
            throw new IllegalStateException("예약 가능한 슬롯이 없습니다: 날짜=" + date + ", 자원=" + resource
                    + ", 필요 분=" + requiredMinutes);
        }

        OptimisticLockException lastOle = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            for (WorkSlot s : candidates) {
                try {
                    int remaining = s.getCapacityMin() - s.getReservedMin();
                    if (remaining < requiredMinutes)
                        continue; // 조회 이후 선점되었을 수 있음
                    s.setReservedMin(s.getReservedMin() + requiredMinutes);
                    slots.saveAndFlush(s); // flush to trigger version increment
                    return new ReservedSlot(s.getId(), s.getWorkDate(), s.getSlotIndex(), s.getResource(),
                            requiredMinutes);
                } catch (OptimisticLockException ole) {
                    lastOle = ole;
                    // 현재 상태를 다시 조회하고 계속 진행
                    // 재시도로 넘어감
                }
            }
            // 경합 이후 후보 목록을 다시 조회
            candidates = slots.findAvailableSlots(date, resource, requiredMinutes);
        }
        if (lastOle != null)
            throw lastOle;
        throw new IllegalStateException("여러 번 재시도했지만 용량 예약에 실패했습니다");
    }
}
