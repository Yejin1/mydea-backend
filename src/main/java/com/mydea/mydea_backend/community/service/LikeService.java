package com.mydea.mydea_backend.community.service;

import com.mydea.mydea_backend.account.repo.AccountRepository;
import com.mydea.mydea_backend.community.domain.*;
import com.mydea.mydea_backend.community.repo.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class LikeService {
    private final CommunityPostRepository posts;
    private final CommunityLikeRepository likes;
    private final AccountRepository accounts;

    @Transactional
    public long like(Long userId, Long postId) {
        CommunityPost post = posts.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("게시물을 찾을 수 없습니다."));
        accounts.findById(userId).orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        if (!likes.existsByPostPostIdAndUserId(postId, userId)) {
            CommunityLike like = new CommunityLike();
            like.setId(new CommunityLikeKey(postId, userId));
            like.setPost(post);
            like.setUser(accounts.getReferenceById(userId));
            likes.save(like);
            post.setLikeCount(post.getLikeCount() + 1);
        }
        return post.getLikeCount();
    }

    @Transactional
    public long unlike(Long userId, Long postId) {
        CommunityPost post = posts.findById(postId)
                .orElseThrow(() -> new NoSuchElementException("게시물을 찾을 수 없습니다."));
        if (likes.existsByPostPostIdAndUserId(postId, userId)) {
            likes.deleteByPostPostIdAndUserId(postId, userId);
            post.setLikeCount(Math.max(0, post.getLikeCount() - 1));
        }
        return post.getLikeCount();
    }
}
