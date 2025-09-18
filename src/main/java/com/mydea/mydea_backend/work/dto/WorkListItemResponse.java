package com.mydea.mydea_backend.work.dto;

import com.mydea.mydea_backend.work.domain.Work;
import java.time.Instant;

public record WorkListItemResponse(
        Long id,
        String name,
        String workType,
        String designType,
        String signedPreviewUrl, //읽기 전용 SAS가 붙은 URL
        Instant createdAt,
        Instant updatedAt
) {
    public static WorkListItemResponse of(Work w, String signedPreviewUrl) {
        return new WorkListItemResponse(
                w.getId(),
                w.getName(),
                w.getWorkType().name(),
                w.getDesignType().name(),
                signedPreviewUrl,
                w.getCreatedAt(),
                w.getUpdatedAt()
        );
    }
}
