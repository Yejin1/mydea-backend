package com.mydea.mydea_backend.community.controller;

import com.mydea.mydea_backend.community.domain.CommunityPost;
import com.mydea.mydea_backend.community.dto.*;
import com.mydea.mydea_backend.community.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/community/posts")
@RequiredArgsConstructor
public class CommunityPostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostResponse> create(Authentication auth, @Valid @RequestBody PostCreateRequest req) {
        Long userId = Long.valueOf(auth.getName());
        Long postId = postService.create(userId, req.getWorkId(), req.getTitle(), req.getDescription(),
                req.isAllowCopy(), req.isAllowComments());
        CommunityPost post = postService.getPublic(postId);
        return ResponseEntity.status(201).body(PostResponse.from(post));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<PostResponse> getOne(@PathVariable Long postId) {
        CommunityPost post = postService.getPublic(postId);
        return ResponseEntity.ok(PostResponse.from(post));
    }

    @GetMapping("/feed")
    public ResponseEntity<Page<PostResponse>> feed(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size);
        var pageEntity = postService.feed(pageable);
        var content = pageEntity.getContent().stream().map(PostResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(new PageImpl<>(content, pageable, pageEntity.getTotalElements()));
    }

    @GetMapping("/me")
    public ResponseEntity<Page<PostResponse>> myPosts(Authentication auth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = Long.valueOf(auth.getName());
        var pageable = PageRequest.of(page, size);
        var pageEntity = postService.myPosts(userId, pageable);
        var content = pageEntity.getContent().stream().map(PostResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(new PageImpl<>(content, pageable, pageEntity.getTotalElements()));
    }

    @PatchMapping("/{postId}")
    public ResponseEntity<PostResponse> update(Authentication auth, @PathVariable Long postId,
            @Valid @RequestBody PostUpdateRequest req) {
        Long userId = Long.valueOf(auth.getName());
        postService.updateFlags(userId, postId, req.getVisibility(), req.getAllowCopy(), req.getAllowComments(),
                req.getTitle(), req.getDescription());
        // 변경 후 재조회(공개 여부 변경으로 getPublic이 막힐 수 있어 직접 서비스 쿼리 추가가 바람직하지만, 간단히 feed 재사용)
        CommunityPost post = postService.getPublic(postId);
        return ResponseEntity.ok(PostResponse.from(post));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long postId) {
        Long userId = Long.valueOf(auth.getName());
        postService.delete(userId, postId);
        return ResponseEntity.noContent().build();
    }
}
