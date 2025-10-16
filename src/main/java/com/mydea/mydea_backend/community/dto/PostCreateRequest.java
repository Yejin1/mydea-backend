package com.mydea.mydea_backend.community.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PostCreateRequest {
    @NotNull
    private Long workId;

    @Size(max = 200)
    private String title;

    private String description; // 자유 텍스트

    private boolean allowCopy = false;
    private boolean allowComments = true;
}
