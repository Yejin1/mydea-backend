package com.mydea.mydea_backend.work.dto;

import com.mydea.mydea_backend.work.domain.Work;
import java.time.Instant;

public record WorkListItemResponse(
        Long id,
        String name,
        String previewUrl,
        String workType,
        String designType,
        Instant createdAt,
        Instant updatedAt
) {
    public static WorkListItemResponse from(Work w) {
        return new WorkListItemResponse(
                w.getId(),
                w.getName(),
                w.getPreviewUrl(),
                w.getWorkType().name(),
                w.getDesignType().name(),
                w.getCreatedAt(),
                w.getUpdatedAt()
        );
    }
}