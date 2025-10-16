package com.mydea.mydea_backend.community.domain;

import com.mydea.mydea_backend.account.domain.Account;
import com.mydea.mydea_backend.work.domain.Work;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "community_post", indexes = {
        @Index(name = "idx_post_visibility_published", columnList = "visibility, published_at"),
        @Index(name = "idx_post_author", columnList = "author_id, created_at"),
        @Index(name = "idx_post_work", columnList = "work_id")
})
public class CommunityPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "work_id", nullable = false)
    private Work work;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Account author;

    @Column(name = "title", length = 200)
    private String title;

    @Lob
    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", length = 16, nullable = false)
    @Builder.Default
    private CommunityVisibility visibility = CommunityVisibility.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 16, nullable = false)
    @Builder.Default
    private CommunityPostStatus status = CommunityPostStatus.ACTIVE;

    @Column(name = "allow_copy", nullable = false)
    @Builder.Default
    private boolean allowCopy = false;

    @Column(name = "allow_comments", nullable = false)
    @Builder.Default
    private boolean allowComments = true;

    @Column(name = "published_at", nullable = false)
    private LocalDateTime publishedAt;

    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Integer likeCount = 0;

    @Column(name = "comment_count", nullable = false)
    @Builder.Default
    private Integer commentCount = 0;

    // Snapshot
    @Column(name = "snapshot_name", length = 200)
    private String snapshotName;

    @Column(name = "snapshot_thumb_url", length = 512)
    private String snapshotThumbUrl;

    @Lob
    @Column(name = "snapshot_payload")
    private String snapshotPayload; // JSON text

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (this.publishedAt == null) {
            this.publishedAt = LocalDateTime.now();
        }
    }
}
