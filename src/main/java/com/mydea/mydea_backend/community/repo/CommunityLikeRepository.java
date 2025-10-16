package com.mydea.mydea_backend.community.repo;

import com.mydea.mydea_backend.community.domain.CommunityLike;
import com.mydea.mydea_backend.community.domain.CommunityLikeKey;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityLikeRepository extends JpaRepository<CommunityLike, CommunityLikeKey> {
    boolean existsByPostPostIdAndUserId(Long postId, Long userId);

    long countByPostPostId(Long postId);

    void deleteByPostPostIdAndUserId(Long postId, Long userId);
}
