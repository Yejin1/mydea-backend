package com.mydea.mydea_backend.work.repo;

import com.mydea.mydea_backend.work.domain.Work;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkRepository extends JpaRepository<Work, Long> {
    Page<Work> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}