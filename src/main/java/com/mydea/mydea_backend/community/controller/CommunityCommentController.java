package com.mydea.mydea_backend.community.controller;

import com.mydea.mydea_backend.community.dto.CommentCreateRequest;
import com.mydea.mydea_backend.community.dto.CommentResponse;
import com.mydea.mydea_backend.community.service.CommentService;
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
@RequestMapping("/api/community/posts/{postId}/comments")
@RequiredArgsConstructor
public class CommunityCommentController {
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> add(Authentication auth, @PathVariable Long postId,
            @Valid @RequestBody CommentCreateRequest req) {
        Long userId = Long.valueOf(auth.getName());
        var c = commentService.add(userId, postId, req.getContent(), req.getParentId());
        return ResponseEntity.status(201).body(CommentResponse.from(c));
    }

    @GetMapping
    public ResponseEntity<Page<CommentResponse>> list(@PathVariable Long postId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size);
        var pageEntity = commentService.list(postId, pageable);
        var content = pageEntity.getContent().stream().map(CommentResponse::from).collect(Collectors.toList());
        return ResponseEntity.ok(new PageImpl<>(content, pageable, pageEntity.getTotalElements()));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(Authentication auth, @PathVariable Long postId, @PathVariable Long commentId) {
        Long userId = Long.valueOf(auth.getName());
        commentService.delete(userId, commentId);
        return ResponseEntity.noContent().build();
    }
}
