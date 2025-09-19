package com.mydea.mydea_backend.work.service;

import com.mydea.mydea_backend.storage.BlobSasService;
import com.mydea.mydea_backend.work.domain.Work;
import com.mydea.mydea_backend.work.dto.WorkRequest;
import com.mydea.mydea_backend.work.dto.WorkUpdateRequest;
import com.mydea.mydea_backend.work.repo.WorkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class WorkServiceTest {

    @Mock
    WorkRepository workRepository;

    @Mock
    BlobSasService blobSasService;

    @InjectMocks
    WorkService workService;

    @Captor
    ArgumentCaptor<Work> workCaptor;

    private WorkRequest validCreateReqRingManualWithRadius;
    private WorkRequest validCreateReqFlower;

    @BeforeEach
    void setUp() {
        // ring + 수동(autoSize=0) + radiusMm 제공 → 유효
        validCreateReqRingManualWithRadius = new WorkRequest(
                1L,
                "my ring",
                Work.WorkType.ring,
                Work.DesignType.basic,
                List.of("#ff0000", "#00ff00"),
                "#0000ff",
                "#ffff00",
                0,
                new BigDecimal("15.500"),
                null
        );

        // flower 디자인 → petal/center 필수 포함 → 유효
        validCreateReqFlower = new WorkRequest(
                2L,
                "flower bracelet",
                Work.WorkType.bracelet,
                Work.DesignType.flower,
                List.of("#123456"),
                "#aaaaaa",
                "#bbbbbb",
                1,
                null,
                2
        );
    }

    // ---------- create ----------
    @Test
    @DisplayName("create 성공: ring 수동 + radiusMm 제공")
    void create_success_ring_manual_with_radius() {
        given(workRepository.save(any(Work.class))).willAnswer(invocation -> {
            Work arg = invocation.getArgument(0);
            arg.setId(100L);
            return arg;
        });

        Work saved = workService.create(validCreateReqRingManualWithRadius);

        assertNotNull(saved);
        assertEquals(100L, saved.getId());
        then(workRepository).should().save(workCaptor.capture());
        Work toSave = workCaptor.getValue();
        assertEquals("my ring", toSave.getName());
        assertEquals(Work.WorkType.ring, toSave.getWorkType());
        assertEquals(Work.DesignType.basic, toSave.getDesignType());
        assertEquals(List.of("#ff0000", "#00ff00"), toSave.getColors());
        assertEquals(new BigDecimal("15.500"), toSave.getRadiusMm());
        assertNull(toSave.getSizeIndex());
    }

    @Test
    @DisplayName("create 성공: flower 디자인은 petal/center 필수")
    void create_success_flower_requires_colors() {
        given(workRepository.save(any(Work.class))).willAnswer(invocation -> {
            Work arg = invocation.getArgument(0);
            arg.setId(101L);
            return arg;
        });

        Work saved = workService.create(validCreateReqFlower);

        assertEquals(101L, saved.getId());
        then(workRepository).should().save(any(Work.class));
    }

    @Test
    @DisplayName("create 실패: flower인데 petal/center 누락")
    void create_fail_flower_missing_colors() {
        WorkRequest bad = new WorkRequest(
                3L, "bad flower", Work.WorkType.necklace, Work.DesignType.flower,
                List.of("#abcdef"), null, null, 1, null, null
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> workService.create(bad));
        assertTrue(ex.getMessage().contains("flower"));
        then(workRepository).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("create 실패: ring 수동인데 radius/sizeIndex 모두 없음")
    void create_fail_ring_manual_missing_size() {
        WorkRequest bad = new WorkRequest(
                4L, "bad ring", Work.WorkType.ring, Work.DesignType.basic,
                List.of("#abcdef"), "#000000", "#ffffff", 0, null, null
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> workService.create(bad));
        assertTrue(ex.getMessage().toLowerCase().contains("ring"));
        then(workRepository).shouldHaveNoInteractions();
    }

    // ---------- update ----------
    @Test
    @DisplayName("update 성공: 전체 필드 갱신 후 save")
    void update_success() {
        Long id = 10L;
        Work existing = Work.builder()
                .id(id)
                .userId(1L)
                .name("old")
                .workType(Work.WorkType.ring)
                .designType(Work.DesignType.basic)
                .colors(List.of("#000000"))
                .autoSize(1)
                .build();

        given(workRepository.findById(id)).willReturn(Optional.of(existing));
        given(workRepository.save(any(Work.class))).willAnswer(invocation -> invocation.getArgument(0));

        WorkUpdateRequest req = new WorkUpdateRequest(
                "updated",
                Work.WorkType.bracelet,
                Work.DesignType.flower,
                List.of("#123456", "#abcdef"),
                "#aaaaaa",
                "#bbbbbb",
                1,
                new BigDecimal("20.000"),
                3
        );

        Work updated = workService.update(id, req);

        assertEquals("updated", updated.getName());
        assertEquals(Work.WorkType.bracelet, updated.getWorkType());
        assertEquals(Work.DesignType.flower, updated.getDesignType());
        assertEquals(List.of("#123456", "#abcdef"), updated.getColors());
        assertEquals("#aaaaaa", updated.getFlowerPetal());
        assertEquals("#bbbbbb", updated.getFlowerCenter());
        assertEquals(1, updated.getAutoSize());
        assertEquals(new BigDecimal("20.000"), updated.getRadiusMm());
        assertEquals(3, updated.getSizeIndex());
        then(workRepository).should().save(existing);
    }

    @Test
    @DisplayName("update 실패: 존재하지 않는 id")
    void update_fail_not_found() {
        given(workRepository.findById(999L)).willReturn(Optional.empty());

        WorkUpdateRequest req = new WorkUpdateRequest(
                "x",
                Work.WorkType.ring,
                Work.DesignType.basic,
                List.of("#111111"),
                "#222222",
                "#333333",
                1,
                null,
                null
        );

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> workService.update(999L, req));
        assertTrue(ex.getMessage().contains("작업물 없음"));
        then(workRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("update 실패: flower인데 petal/center 누락")
    void update_fail_flower_missing_colors() {
        Long id = 11L;
        Work existing = Work.builder().id(id).workType(Work.WorkType.bracelet).designType(Work.DesignType.basic).autoSize(1).build();
        given(workRepository.findById(id)).willReturn(Optional.of(existing));

        WorkUpdateRequest bad = new WorkUpdateRequest(
                "name",
                Work.WorkType.bracelet,
                Work.DesignType.flower,
                List.of("#aaaaaa"),
                null,
                null,
                1,
                null,
                null
        );

        assertThrows(IllegalArgumentException.class, () -> workService.update(id, bad));
        then(workRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("update 실패: ring 수동인데 radius/sizeIndex 모두 없음")
    void update_fail_ring_manual_missing_size() {
        Long id = 12L;
        Work existing = Work.builder().id(id).workType(Work.WorkType.ring).designType(Work.DesignType.basic).autoSize(1).build();
        given(workRepository.findById(id)).willReturn(Optional.of(existing));

        WorkUpdateRequest bad = new WorkUpdateRequest(
                "name",
                Work.WorkType.ring,
                Work.DesignType.basic,
                List.of("#aaaaaa"),
                "#111111",
                "#222222",
                0,          // 수동
                null,       // radius 없음
                null        // sizeIndex 없음
        );

        assertThrows(IllegalArgumentException.class, () -> workService.update(id, bad));
        then(workRepository).should(never()).save(any());
    }

    // ---------- deleteWorks ----------
    @Test
    @DisplayName("deleteWorks: repository 호출 검증")
    void deleteWorks_calls_repository() {
        List<Long> ids = List.of(1L, 2L, 3L);

        Work w1 = Work.builder().id(1L).previewUrl("https://blob/1.png").build();
        Work w2 = Work.builder().id(2L).previewUrl(null).build();
        Work w3 = Work.builder().id(3L).previewUrl("https://blob/3.png").build();

        given(workRepository.findAllById(ids)).willReturn(List.of(w1, w2, w3));

        workService.deleteWorks(ids);

        then(workRepository).should().findAllById(ids);
        then(workRepository).should().deleteAllByIdIn(ids);
        then(workRepository).shouldHaveNoMoreInteractions();
        // Blob 삭제는 TODO로 주석 처리되어 있으므로 여기선 검증하지 않음
    }

    // ---------- updatePreviewUrl ----------
    @Test
    @DisplayName("updatePreviewUrl 성공: URL 저장")
    void updatePreviewUrl_success() {
        Long id = 5L;
        Work existing = Work.builder().id(id).previewUrl(null).build();
        given(workRepository.findById(id)).willReturn(Optional.of(existing));
        given(workRepository.save(any(Work.class))).willAnswer(invocation -> invocation.getArgument(0));

        workService.updatePreviewUrl(id, "https://blob/works/5/preview.png");

        assertEquals("https://blob/works/5/preview.png", existing.getPreviewUrl());
        then(workRepository).should().save(existing);
    }

    @Test
    @DisplayName("updatePreviewUrl 실패: 존재하지 않는 id")
    void updatePreviewUrl_fail_not_found() {
        given(workRepository.findById(404L)).willReturn(Optional.empty());

        NoSuchElementException ex = assertThrows(NoSuchElementException.class,
                () -> workService.updatePreviewUrl(404L, "x"));
        assertTrue(ex.getMessage().contains("작업물 없음"));
        then(workRepository).should(never()).save(any());
    }
}
