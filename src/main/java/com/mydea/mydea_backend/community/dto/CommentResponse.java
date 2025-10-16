package com.mydea.mydea_backend.community.dto;

import com.mydea.mydea_backend.community.domain.CommunityComment;
import com.mydea.mydea_backend.community.domain.CommunityCommentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CommentResponse {
    private Long commentId;
    private Long postId;
    private Long userId;
    private String userNickname;
    private String content;
    private Long parentId;
    private CommunityCommentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CommentResponse from(CommunityComment c) {
        return CommentResponse.builder()
                .commentId(c.getCommentId())
                .postId(c.getPost().getPostId())
                .userId(c.getUser().getId())
                .userNickname(c.getUser().getNickname())
                .content(c.getContent())
                .parentId(c.getParent() != null ? c.getParent().getCommentId() : null)
                .status(c.getStatus())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
