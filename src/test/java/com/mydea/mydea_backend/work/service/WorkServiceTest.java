package com.mydea.mydea_backend.work.service;

import com.mydea.mydea_backend.work.domain.Work;
import com.mydea.mydea_backend.work.repo.WorkRepository;
import com.mydea.mydea_backend.work.dto.WorkRequest;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class WorkServiceTest {

    private final WorkRepository workRepository = mock(WorkRepository.class);
    private final WorkService workService = new WorkService(workRepository);

    @Test
    void create_flower_requires_flower_colors() {
        WorkRequest req = new WorkRequest(
                1L, "플라워 반지",
                Work.WorkType.ring,
                Work.DesignType.flower,
                List.of("#aaaaaa"),
                null, null, 0,
                new BigDecimal("8.0"), 1
        );
        assertThatThrownBy(() -> workService.create(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("flower 디자인은");
    }

    @Test
    void ring_manual_requires_radius_or_sizeIndex() {
        WorkRequest req = new WorkRequest(
                1L, "수동 링",
                Work.WorkType.ring,
                Work.DesignType.basic,
                List.of("#aaaaaa"),
                null, null, 0,
                null, null
        );
        assertThatThrownBy(() -> workService.create(req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ring 수동 사이즈");
    }

    @Test
    void create_ok_persists_to_repository() {
        WorkRequest req = new WorkRequest(
                1L, "정상 케이스",
                Work.WorkType.ring,
                Work.DesignType.flower,
                List.of("#feadad", "#a1d9a4"),
                "#ffb6c1", "#ffe066", 0,
                new BigDecimal("8.4"), 2
        );
        when(workRepository.save(any())).thenAnswer(inv -> {
            Work w = inv.getArgument(0);
            w.setId(100L);
            return w;
        });

        Work saved = workService.create(req);
        assertThat(saved.getId()).isEqualTo(100L);

        ArgumentCaptor<Work> cap = ArgumentCaptor.forClass(Work.class);
        verify(workRepository).save(cap.capture());
        Work toSave = cap.getValue();
        assertThat(toSave.getColors()).containsExactly("#feadad", "#a1d9a4");
        assertThat(toSave.getRadiusMm()).isEqualByComparingTo("8.4");
        assertThat(toSave.getSizeIndex()).isEqualTo(2);
    }
}
