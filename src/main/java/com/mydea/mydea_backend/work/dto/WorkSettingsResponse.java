package com.mydea.mydea_backend.work.dto;

import com.mydea.mydea_backend.work.domain.Work;
import java.math.BigDecimal;
import java.util.List;

public record WorkSettingsResponse(
        Long id,
        Long userId,
        String name,
        String accessory,              // "ring" | "bracelet" | "necklace"
        String design,                 // "basic" | "flower"
        List<String> colors,           // ["#feadad", ...]
        FlowerColors flowerColors,     // {petal:"#...", center:"#..."}
        Integer autoSize,
        BigDecimal radiusMm,
        Integer sizeIndex
) {
    public record FlowerColors(String petal, String center) {}
    public static WorkSettingsResponse from(Work w) {
        return new WorkSettingsResponse(
                w.getId(),
                w.getUserId(),
                w.getName(),
                w.getWorkType().name(),
                w.getDesignType().name(),
                w.getColors(),
                new FlowerColors(w.getFlowerPetal(), w.getFlowerCenter()),
                w.getAutoSize(),
                w.getRadiusMm(),
                w.getSizeIndex()
        );
    }
}
