package com.mydea.mydea_backend.community.service;

import com.mydea.mydea_backend.account.repo.AccountRepository;
import com.mydea.mydea_backend.community.domain.*;
import com.mydea.mydea_backend.community.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommunityPostRepository posts;
    private final CommunityCommentRepository comments;
    private final AccountRepository accounts;

    @Transactional
    public CommunityComment add(Long userId, Long postId, String content, Long parentId) {
        CommunityPost post = posts.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("게시물을 찾을 수 없습니다."));
        if (!post.isAllowComments())
            throw new IllegalStateException("댓글이 비활성화된 게시물입니다.");

        var user = accounts.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        CommunityComment c = CommunityComment.builder()
                .post(post)
                .user(user)
                .content(content)
                .status(CommunityCommentStatus.VISIBLE)
                .build();
        if (parentId != null) {
            CommunityComment parent = comments.findById(parentId)
                    .orElseThrow(() -> new NoSuchElementException("부모 댓글을 찾을 수 없습니다."));
            c.setParent(parent);
        }
        comments.save(c);
        post.setCommentCount(post.getCommentCount() + 1);
        return c;
    }

    @Transactional(readOnly = true)
    public Page<CommunityComment> list(Long postId, Pageable pageable) {
        return comments.findByPostPostIdAndStatusOrderByCreatedAtAsc(postId, CommunityCommentStatus.VISIBLE, pageable);
    }

    @Transactional
    public void delete(Long userId, Long commentId) {
        CommunityComment c = comments.findById(commentId)
                .orElseThrow(() -> new NoSuchElementException("댓글을 찾을 수 없습니다."));
        if (!c.getUser().getId().equals(userId) && !c.getPost().getAuthor().getId().equals(userId))
            throw new SecurityException("삭제 권한이 없습니다.");
        if (c.getStatus() != CommunityCommentStatus.DELETED) {
            c.setStatus(CommunityCommentStatus.DELETED);
            CommunityPost post = c.getPost();
            post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
        }
    }
}
