package com.mydea.mydea_backend.work.repo;

import com.mydea.mydea_backend.work.domain.Work;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkRepository extends JpaRepository<Work, Long> {
}