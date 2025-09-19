package com.mydea.mydea_backend.work.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mydea.mydea_backend.storage.BlobSasService;
import com.mydea.mydea_backend.work.domain.Work;
import com.mydea.mydea_backend.work.dto.WorkRequest;
import com.mydea.mydea_backend.work.dto.WorkUpdateRequest;
import com.mydea.mydea_backend.work.service.WorkQueryService;
import com.mydea.mydea_backend.work.service.WorkService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WithMockUser(username = "tester", roles = "USER")
@WebMvcTest(controllers = WorkController.class)
class WorkControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @MockitoBean
    WorkService workService;

    @MockitoBean
    WorkQueryService workQueryService;

    @MockitoBean
    BlobSasService blobSasService;

    private String json(Object o) throws Exception {
        return om.writeValueAsString(o);
    }

    private Work stubWork(Long id, String previewUrl) {
        Work w = mock(Work.class);
        // 엔티티가 final이 아니라고 가정 (일반적인 JPA @Entity)
        org.mockito.Mockito.when(w.getId()).thenReturn(id);
        org.mockito.Mockito.when(w.getPreviewUrl()).thenReturn(previewUrl);
        return w;
    }

    private Work makeWork(
            Long id,
            String name,
            Work.WorkType workType,
            Work.DesignType designType,
            List<String> colors,
            String flowerPetal,
            String flowerCenter,
            Integer autoSize,
            BigDecimal radiusMm,
            Integer sizeIndex,
            String previewUrl
    ) {
        Work w = Work.builder()
                .id(id)
                .userId(1L)
                .name(name)
                .workType(workType)
                .designType(designType)
                .colors(colors)
                .flowerPetal(flowerPetal)
                .flowerCenter(flowerCenter)
                .autoSize(autoSize)
                .radiusMm(radiusMm)
                .sizeIndex(sizeIndex)
                .previewUrl(previewUrl)
                .build();
        return w;
    }


    // ---------- POST /api/works ----------
    @Test
    @DisplayName("POST /api/works - create")
    void create() throws Exception {
        // WorkRequest 유효 값 세팅 (DTO 제약 충족)
        WorkRequest req = new WorkRequest(
                1L,                                   // userId
                "my ring",                            // name (max 200)
                Work.WorkType.ring,                   // @NotNull
                Work.DesignType.basic,                // @NotNull
                List.of("#ff0000", "#00ff00"),        // @Size(min=1) & hex color 패턴
                "#0000ff",                            // flowerPetal (hex)
                "#ffff00",                            // flowerCenter (hex)
                0,                                    // autoSize @NotNull
                new BigDecimal("15.500"),             // @Digits(integer=5, fraction=3)
                2                                     // sizeIndex
        );

        Work saved = makeWork(
                100L,
                "my ring",
                Work.WorkType.ring,
                Work.DesignType.basic,
                List.of("#ff0000", "#00ff00"),
                "#0000ff",
                "#ffff00",
                0,
                new BigDecimal("15.500"),
                2,
                "https://blob/works/100/preview.png"
        );
        given(workService.create(any(WorkRequest.class))).willReturn(saved);

        mvc.perform(post("/api/works")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", notNullValue()));
    }

    // ---------- DELETE /api/works ----------
    @Test
    @DisplayName("DELETE /api/works - bulk delete")
    void deleteBulk() throws Exception {
        List<Long> ids = List.of(1L, 2L, 3L);
        doNothing().when(workService).deleteWorks(eq(ids));

        mvc.perform(delete("/api/works")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(ids)))
                .andExpect(status().isNoContent());
    }

    // ---------- PATCH /api/works/{id}/preview-url ----------
    @Test
    @DisplayName("PATCH /api/works/{id}/preview-url - 미리보기 URL 갱신")
    void patchPreviewUrl() throws Exception {
        Long id = 77L;
        Map<String, String> body = Map.of("previewUrl", "https://blob/works/77/preview.png");
        doNothing().when(workService).updatePreviewUrl(eq(id), eq(body.get("previewUrl")));

        mvc.perform(patch("/api/works/{id}/preview-url", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(body)))
                .andExpect(status().isNoContent());
    }

    // ---------- GET /api/works/{id}/preview-signed-url ----------
    @Nested
    @DisplayName("GET /api/works/{id}/preview-signed-url")
    class SignedPreviewUrl {

        @Test
        @DisplayName("canonical 존재 시 SAS 발급")
        void ok_when_canonical_exists() throws Exception {
            Long id = 5L;
            Work w = stubWork(id, "https://blob/works/5/preview.png");

            given(workQueryService.getOrThrow(eq(id), isNull())).willReturn(w);
            given(blobSasService.issueReadSasUrl(eq("https://blob/works/5/preview.png"), eq(Duration.ofHours(1))))
                    .willReturn("https://signed.example/works/5/preview.png?sig=abc");

            mvc.perform(get("/api/works/{id}/preview-signed-url", id))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.url", startsWith("https://signed.example/")))
                    .andExpect(jsonPath("$.expiresIn").value("3600"));
        }

        @Test
        @DisplayName("404: canonical 미설정(null)")
        void not_found_when_canonical_null() throws Exception {
            Long id = 6L;
            Work w = stubWork(id, null);

            given(workQueryService.getOrThrow(eq(id), isNull())).willReturn(w);

            mvc.perform(get("/api/works/{id}/preview-signed-url", id))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("404: canonical 공백")
        void not_found_when_canonical_blank() throws Exception {
            Long id = 7L;
            Work w = stubWork(id, "   ");

            given(workQueryService.getOrThrow(eq(id), isNull())).willReturn(w);

            mvc.perform(get("/api/works/{id}/preview-signed-url", id))
                    .andExpect(status().isNotFound());
        }
    }

    // ---------- PUT /api/works/{id} ----------
    @Test
    @DisplayName("PUT /api/works/{id} - update 성공")
    void update() throws Exception {
        Long id = 10L;

        WorkUpdateRequest req = new WorkUpdateRequest(
                "updated name",                       // name
                Work.WorkType.bracelet,              // @NotNull
                Work.DesignType.flower,              // @NotNull
                List.of("#123456", "#abcdef"),       // @Size(min=1), hex pattern
                "#aaaaaa",                           // flowerPetal
                "#bbbbbb",                           // flowerCenter
                1,                                   // autoSize @NotNull
                new BigDecimal("20.000"),            // radiusMm @Digits
                3                                    // sizeIndex
        );

        Work updated = makeWork(
                id,
                "updated",
                Work.WorkType.bracelet,
                Work.DesignType.flower,
                List.of("#123456", "#abcdef"),
                "#aaaaaa",
                "#bbbbbb",
                1,
                new BigDecimal("20.000"),
                3,
                "https://blob/works/10/preview.png"
        );
        given(workService.update(eq(id), any(WorkUpdateRequest.class))).willReturn(updated);

        mvc.perform(put("/api/works/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(req)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id));
    }
}