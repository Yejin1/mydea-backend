package com.mydea.mydea_backend.community.repo;

import com.mydea.mydea_backend.community.domain.CommunityComment;
import com.mydea.mydea_backend.community.domain.CommunityCommentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommunityCommentRepository extends JpaRepository<CommunityComment, Long> {
    Page<CommunityComment> findByPostPostIdAndStatusOrderByCreatedAtAsc(Long postId, CommunityCommentStatus status,
            Pageable pageable);

    List<CommunityComment> findByParentCommentId(Long parentId);

    long countByPostPostIdAndStatus(Long postId, CommunityCommentStatus status);
}
