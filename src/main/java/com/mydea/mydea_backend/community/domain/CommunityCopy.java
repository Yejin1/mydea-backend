package com.mydea.mydea_backend.community.domain;

import com.mydea.mydea_backend.account.domain.Account;
import com.mydea.mydea_backend.work.domain.Work;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "community_copy", indexes = {
        @Index(name = "idx_copy_copier", columnList = "copier_id, created_at")
})
public class CommunityCopy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "copy_id")
    private Long copyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private CommunityPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_work_id", nullable = false)
    private Work sourceWork;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dest_work_id", nullable = false)
    private Work destWork;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "copier_id", nullable = false)
    private Account copier;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
