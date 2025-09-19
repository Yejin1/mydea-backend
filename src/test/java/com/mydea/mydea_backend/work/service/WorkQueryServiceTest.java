package com.mydea.mydea_backend.work.service;

import com.mydea.mydea_backend.storage.BlobSasService;
import com.mydea.mydea_backend.work.domain.Work;
import com.mydea.mydea_backend.work.repo.WorkRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class WorkQueryServiceTest {

    @Mock
    WorkRepository workRepository;

    @Mock
    BlobSasService blobSasService;

    @InjectMocks
    WorkQueryService workQueryService;

    @Captor
    ArgumentCaptor<Pageable> pageableCaptor;

    // ---- helpers ----
    private Work makeWork(Long id, Long userId, String previewUrl) {
        return Work.builder()
                .id(id)
                .userId(userId)
                .name("n")
                .workType(Work.WorkType.ring)
                .designType(Work.DesignType.basic)
                .colors(List.of("#111111"))
                .flowerPetal("#000000")
                .flowerCenter("#ffffff")
                .autoSize(1)
                .radiusMm(new BigDecimal("10.000"))
                .sizeIndex(1)
                .previewUrl(previewUrl)
                .build();
    }

    // ---------- listByUser ----------
    @Test
    @DisplayName("listByUser: userId, page, size로 조회하고 createdAt DESC 정렬로 Pageable을 전달한다")
    void listByUser_uses_pageable_and_sort() {
        // given
        Long userId = 7L;
        given(workRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(makeWork(1L, userId, null))));

        // when
        Page<Work> page = workQueryService.listByUser(userId, 2, 20);

        // then
        assertThat(page.getContent()).hasSize(1);
        then(workRepository).should().findByUserIdOrderByCreatedAtDesc(eq(userId), pageableCaptor.capture());
        Pageable p = pageableCaptor.getValue();
        assertThat(p.getPageNumber()).isEqualTo(2);
        assertThat(p.getPageSize()).isEqualTo(20);

        Sort.Order order = p.getSort().getOrderFor("createdAt");
        assertThat(order).isNotNull();
        assertThat(order.getDirection()).isEqualTo(Sort.Direction.DESC);
    }

    // ---------- signPreviewUrlOrNull ----------
    @Test
    @DisplayName("signPreviewUrlOrNull: previewUrl이 null이면 null 반환 & SAS 미발급")
    void signPreviewUrlOrNull_returns_null_when_null() {
        Work w = makeWork(1L, 1L, null);

        String signed = workQueryService.signPreviewUrlOrNull(w);

        assertNull(signed);
        then(blobSasService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("signPreviewUrlOrNull: previewUrl이 공백이면 null 반환 & SAS 미발급")
    void signPreviewUrlOrNull_returns_null_when_blank() {
        Work w = makeWork(1L, 1L, "   ");

        String signed = workQueryService.signPreviewUrlOrNull(w);

        assertNull(signed);
        then(blobSasService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("signPreviewUrlOrNull: previewUrl이 있으면 TTL=1시간으로 SAS 발급")
    void signPreviewUrlOrNull_issues_sas_with_one_hour_ttl() {
        Work w = makeWork(1L, 1L, "https://blob/works/1/preview.png");
        given(blobSasService.issueReadSasUrl(eq(w.getPreviewUrl()), argThat(d -> Duration.ofHours(1).equals(d))))
                .willReturn("https://signed/url?sig=abc");

        String signed = workQueryService.signPreviewUrlOrNull(w);

        assertEquals("https://signed/url?sig=abc", signed);
        then(blobSasService).should().issueReadSasUrl(eq("https://blob/works/1/preview.png"),
                argThat(d -> Duration.ofHours(1).equals(d)));
    }

    // ---------- getOrThrow ----------
    @Test
    @DisplayName("getOrThrow: userId 검증 없이 id로 조회 성공")
    void getOrThrow_found_no_user_check() {
        Work w = makeWork(10L, 33L, null);
        given(workRepository.findById(10L)).willReturn(Optional.of(w));

        Work got = workQueryService.getOrThrow(10L, null);

        assertSame(w, got);
    }

    @Test
    @DisplayName("getOrThrow: userId가 일치하면 반환")
    void getOrThrow_found_user_matches() {
        Work w = makeWork(11L, 99L, null);
        given(workRepository.findById(11L)).willReturn(Optional.of(w));

        Work got = workQueryService.getOrThrow(11L, 99L);

        assertSame(w, got);
    }

    @Test
    @DisplayName("getOrThrow: 존재하지 않으면 NoSuchElementException")
    void getOrThrow_not_found() {
        given(workRepository.findById(404L)).willReturn(Optional.empty());

        NoSuchElementException ex =
                assertThrows(NoSuchElementException.class, () -> workQueryService.getOrThrow(404L, null));
        assertTrue(ex.getMessage().contains("id=404"));
    }

    @Test
    @DisplayName("getOrThrow: userId 불일치 시 NoSuchElementException(소유권 위반)")
    void getOrThrow_user_mismatch() {
        Work w = makeWork(12L, 1L, null);
        given(workRepository.findById(12L)).willReturn(Optional.of(w));

        NoSuchElementException ex =
                assertThrows(NoSuchElementException.class, () -> workQueryService.getOrThrow(12L, 2L));
        assertTrue(ex.getMessage().contains("소유"));
    }
}
