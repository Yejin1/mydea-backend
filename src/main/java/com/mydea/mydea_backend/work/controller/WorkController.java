package com.mydea.mydea_backend.work.controller;

import com.mydea.mydea_backend.work.domain.Work;
import com.mydea.mydea_backend.work.service.WorkService;
import com.mydea.mydea_backend.work.dto.WorkRequest;
import com.mydea.mydea_backend.work.dto.WorkResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/works")
public class WorkController {

    private final WorkService workService;

    @PostMapping
    public ResponseEntity<WorkResponse> create(@Valid @RequestBody WorkRequest req) {
        Work saved = workService.create(req);
        return ResponseEntity.ok(WorkResponse.from(saved));
    }
}
