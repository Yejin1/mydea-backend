package com.mydea.mydea_backend.community.repo;

import com.mydea.mydea_backend.community.domain.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityPostRepository extends JpaRepository<CommunityPost, Long> {
    Page<CommunityPost> findByVisibilityAndStatusOrderByPublishedAtDesc(
            CommunityVisibility visibility,
            CommunityPostStatus status,
            Pageable pageable);

    Page<CommunityPost> findByAuthorIdAndStatusOrderByCreatedAtDesc(
            Long authorId,
            CommunityPostStatus status,
            Pageable pageable);

    Optional<CommunityPost> findByPostIdAndStatus(Long postId, CommunityPostStatus status);
}
