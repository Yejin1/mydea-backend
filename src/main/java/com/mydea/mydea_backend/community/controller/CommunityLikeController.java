package com.mydea.mydea_backend.community.controller;

import com.mydea.mydea_backend.community.dto.LikeResponse;
import com.mydea.mydea_backend.community.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/community/posts/{postId}/likes")
@RequiredArgsConstructor
public class CommunityLikeController {
    private final LikeService likeService;

    @PostMapping
    public ResponseEntity<LikeResponse> like(Authentication auth, @PathVariable Long postId) {
        Long userId = Long.valueOf(auth.getName());
        long count = likeService.like(userId, postId);
        return ResponseEntity.ok(new LikeResponse(true, count));
    }

    @DeleteMapping
    public ResponseEntity<LikeResponse> unlike(Authentication auth, @PathVariable Long postId) {
        Long userId = Long.valueOf(auth.getName());
        long count = likeService.unlike(userId, postId);
        return ResponseEntity.ok(new LikeResponse(false, count));
    }
}
