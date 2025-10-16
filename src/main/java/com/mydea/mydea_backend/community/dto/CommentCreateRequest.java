package com.mydea.mydea_backend.community.dto;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CommentCreateRequest {
    @NotBlank
    private String content;

    @Nullable
    private Long parentId; // 대댓글이면 부모 댓글 id
}
