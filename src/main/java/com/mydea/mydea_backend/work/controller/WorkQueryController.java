package com.mydea.mydea_backend.work.controller;

import com.mydea.mydea_backend.work.domain.Work;
import com.mydea.mydea_backend.work.service.WorkQueryService;
import com.mydea.mydea_backend.work.dto.WorkListItemResponse;
import com.mydea.mydea_backend.work.dto.WorkSettingsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @RequestParam Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
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
                                                    @RequestParam(required = false) Long userId) {
        Work w = workQueryService.getOrThrow(id, userId);
        String signed = workQueryService.signPreviewUrlOrNull(w);
        return ResponseEntity.ok(WorkSettingsResponse.of(w, signed));
    }
}
