package com.mydea.mydea_backend.community.dto;

import com.mydea.mydea_backend.community.domain.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PostResponse {
    private Long postId;
    private Long workId;
    private Long authorId;
    private String authorNickname;
    private String title;
    private String description;
    private CommunityVisibility visibility;
    private CommunityPostStatus status;
    private boolean allowCopy;
    private boolean allowComments;
    private LocalDateTime publishedAt;
    private Integer likeCount;
    private Integer commentCount;
    private String snapshotName;
    private String snapshotThumbUrl;
    private String snapshotPayload;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PostResponse from(CommunityPost p) {
        return PostResponse.builder()
                .postId(p.getPostId())
                .workId(p.getWork().getId())
                .authorId(p.getAuthor().getId())
                .authorNickname(p.getAuthor().getNickname())
                .title(p.getTitle())
                .description(p.getDescription())
                .visibility(p.getVisibility())
                .status(p.getStatus())
                .allowCopy(p.isAllowCopy())
                .allowComments(p.isAllowComments())
                .publishedAt(p.getPublishedAt())
                .likeCount(p.getLikeCount())
                .commentCount(p.getCommentCount())
                .snapshotName(p.getSnapshotName())
                .snapshotThumbUrl(p.getSnapshotThumbUrl())
                .snapshotPayload(p.getSnapshotPayload())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
