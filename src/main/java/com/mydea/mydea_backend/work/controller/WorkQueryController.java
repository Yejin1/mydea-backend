package com.mydea.mydea_backend.work.controller;

import com.mydea.mydea_backend.work.domain.Work;
import com.mydea.mydea_backend.work.dto.WorkPresetResponse;
import com.mydea.mydea_backend.work.service.WorkQueryService;
import com.mydea.mydea_backend.work.dto.WorkListItemResponse;
import com.mydea.mydea_backend.work.dto.WorkSettingsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/works")
public class WorkQueryController {

    private final WorkQueryService workQueryService;

    /**
     * 내 작업물 목록
     * GET /api/works?userId=1&page=0&size=20
     */
    @GetMapping
    public ResponseEntity<Page<WorkListItemResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Authentication auth
    ) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        final Long userId;
        try {
            userId = Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid principal");
        }
        Page<Work> result = workQueryService.listByUser(userId, page, size);
        Page<WorkListItemResponse> mapped = result.map(w ->
                WorkListItemResponse.of(w, workQueryService.signPreviewUrlOrNull(w)));
        return ResponseEntity.ok(mapped);
    }
    /**
     * 작업물 상세(설정값)
     * GET /api/works/{id}?userId=1
     */
    @GetMapping("/{id}")
    public ResponseEntity<WorkSettingsResponse> get(@PathVariable Long id,
                                                    Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated");
        }
        final Long userId;
        try {
            userId = Long.valueOf(auth.getName());
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid principal");
        }
        Work w = workQueryService.getOrThrow(id, userId);
        String signed = workQueryService.signPreviewUrlOrNull(w);
        return ResponseEntity.ok(WorkSettingsResponse.of(w, signed));
    }

    @GetMapping("/{id}/preset")
    public ResponseEntity<WorkPresetResponse> getPreset(@PathVariable Long id) {
        Work w = workQueryService.getPreset(id);
        return ResponseEntity.ok(WorkPresetResponse.of(w));
    }
}
