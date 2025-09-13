package com.mydea.mydea_backend.work.dto;

import com.mydea.mydea_backend.work.domain.Work;

import java.math.BigDecimal;
import java.util.List;

public record WorkResponse(
        Long id, Long userId, String name,
        String workType, String designType,
        List<String> colors, String flowerPetal, String flowerCenter,
        Integer autoSize, BigDecimal radiusMm, Integer sizeIndex
) {
    public static WorkResponse from(Work w) {
        return new WorkResponse(
                w.getId(), w.getUserId(), w.getName(),
                w.getWorkType().name(), w.getDesignType().name(),
                w.getColors(), w.getFlowerPetal(), w.getFlowerCenter(),
                w.getAutoSize(), w.getRadiusMm(), w.getSizeIndex()
        );
    }
}