package com.mydea.mydea_backend.work.controller;

import com.mydea.mydea_backend.storage.BlobSasService;
import com.mydea.mydea_backend.work.domain.Work;
import com.mydea.mydea_backend.work.service.WorkQueryService;
import com.mydea.mydea_backend.work.service.WorkService;
import com.mydea.mydea_backend.work.dto.WorkRequest;
import com.mydea.mydea_backend.work.dto.WorkResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.time.Duration;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/works")
public class WorkController {

    private final WorkService workService;
    private final WorkQueryService workQueryService;
    private final BlobSasService blobSasService;

    @PostMapping
    public ResponseEntity<WorkResponse> create(@Valid @RequestBody WorkRequest req) {
        Work saved = workService.create(req);
        return ResponseEntity.ok(WorkResponse.from(saved));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@RequestBody List<Long> ids) {
        workService.deleteWorks(ids);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/preview-url")
    public ResponseEntity<Void> updatePreviewUrl(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        String url = body.get("previewUrl");
        workService.updatePreviewUrl(id, url);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/preview-signed-url")
    public ResponseEntity<Map<String, String>> getSignedPreview(@PathVariable Long id) {
        Work w = workQueryService.getOrThrow(id, /* userId */ null);
        String canonical = w.getPreviewUrl();
        if (canonical == null || canonical.isBlank()) return ResponseEntity.notFound().build();
        String signed = blobSasService.issueReadSasUrl(canonical, Duration.ofHours(1));
        return ResponseEntity.ok(Map.of("url", signed, "expiresIn", "3600"));
    }
}
