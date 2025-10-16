package com.mydea.mydea_backend.community.repo;

import com.mydea.mydea_backend.community.domain.CommunityCopy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommunityCopyRepository extends JpaRepository<CommunityCopy, Long> {
    boolean existsByPostPostIdAndCopierId(Long postId, Long copierId);

    Optional<CommunityCopy> findByPostPostIdAndCopierId(Long postId, Long copierId);

    Page<CommunityCopy> findByCopierIdOrderByCreatedAtDesc(Long copierId, Pageable pageable);
}
