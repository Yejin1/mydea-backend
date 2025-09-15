package com.mydea.mydea_backend.work.service;

import com.mydea.mydea_backend.work.domain.Work;
import com.mydea.mydea_backend.work.repo.WorkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class WorkQueryService {
    private final WorkRepository workRepository;

    public Page<Work> listByUser(Long userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return workRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Work getOrThrow(Long id, Long userId) {
        Work w = workRepository.findById(id).orElseThrow(() ->
                new NoSuchElementException("작업물을 찾을 수 없습니다: id=" + id));
        if (userId != null && !userId.equals(w.getUserId())) {
            // 소유자 불일치 보호 (인증 붙기 전 임시 체크)
            throw new NoSuchElementException("해당 사용자 소유가 아닙니다.");
        }
        return w;
    }
}
