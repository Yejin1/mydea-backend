package com.mydea.mydea_backend.work.dto;

import com.mydea.mydea_backend.work.domain.Work;

import java.math.BigDecimal;
import java.util.List;

public record WorkPresetResponse(
        Long id, Long userId, String name,
        String workType, String designType,
        List<String> colors,
        FlowerColors flowerColors,
        Integer autoSize,
        BigDecimal radiusMm,
        Integer sizeIndex
) {
    public record FlowerColors(String petal, String center) {}
    public static WorkPresetResponse of(Work w) {
        return new WorkPresetResponse(
                w.getId(), w.getUserId(), w.getName(),
                w.getWorkType().name(), w.getDesignType().name(),
                w.getColors(),
                new FlowerColors(w.getFlowerPetal(), w.getFlowerCenter()),
                w.getAutoSize(), w.getRadiusMm(), w.getSizeIndex()
        );
    }
}
