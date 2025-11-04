package com.mydea.mydea_backend.production.service;

import com.mydea.mydea_backend.production.domain.ProductionProductType;
import com.mydea.mydea_backend.production.domain.WorkSlot;
import com.mydea.mydea_backend.production.domain.WorkTimeRangeRule;
import com.mydea.mydea_backend.production.repo.WorkSlotRepository;
import com.mydea.mydea_backend.production.repo.WorkTimeRangeRuleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest(properties = "spring.flyway.enabled=false")
@Import(ProductionServiceTest.Config.class)
class ProductionServiceTest {

    @TestConfiguration
    static class Config {
        @Bean
        ProductionService productionService(WorkTimeRangeRuleRepository rules, WorkSlotRepository slots) {
            return new ProductionService(rules, slots);
        }
    }

    @Autowired
    WorkTimeRangeRuleRepository rules;
    @Autowired
    WorkSlotRepository slots;
    @Autowired
    ProductionService productionService;

    @Test
    @DisplayName("estimateMinutes() returns per-unit * qty by rule")
    void estimateMinutes_ok() {
        rules.save(WorkTimeRangeRule.builder()
                .productType(ProductionProductType.BASIC)
                .sizeMin(new BigDecimal("0"))
                .sizeMax(new BigDecimal("100"))
                .minPerUnit(30)
                .priority(1)
                .build());

        int minutes = productionService.estimateMinutes(ProductionProductType.BASIC, new BigDecimal("10"), 2);
        assertThat(minutes).isEqualTo(60);
    }

    @Test
    @DisplayName("reserveInFirstAvailableSlot() reserves from earliest slot with enough remaining")
    void reserve_ok() {
        LocalDate today = LocalDate.now();
        slots.save(WorkSlot.builder()
                .workDate(today)
                .slotIndex(0)
                .resource("R1")
                .capacityMin(120)
                .reservedMin(0)
                .build());

        ProductionService.ReservedSlot reserved = productionService.reserveInFirstAvailableSlot(today, "R1", 60);
        assertThat(reserved.slotIndex()).isEqualTo(0);
        assertThat(reserved.reservedMinutes()).isEqualTo(60);

        WorkSlot slot = slots.findByWorkDateAndSlotIndexAndResource(today, 0, "R1").orElseThrow();
        assertThat(slot.getReservedMin()).isEqualTo(60);
    }
}
