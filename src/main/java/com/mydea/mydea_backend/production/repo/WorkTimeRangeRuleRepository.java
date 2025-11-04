package com.mydea.mydea_backend.production.repo;

import com.mydea.mydea_backend.production.domain.ProductionProductType;
import com.mydea.mydea_backend.production.domain.WorkTimeRangeRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Optional;

public interface WorkTimeRangeRuleRepository extends JpaRepository<WorkTimeRangeRule, Long> {
    Optional<WorkTimeRangeRule> findFirstByProductTypeAndSizeMinLessThanEqualAndSizeMaxGreaterThanEqualOrderByPriorityAsc(
            ProductionProductType productType,
            BigDecimal sizeForMin,
            BigDecimal sizeForMax);
}
