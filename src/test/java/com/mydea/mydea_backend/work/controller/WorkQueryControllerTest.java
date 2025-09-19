package com.mydea.mydea_backend.work.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mydea.mydea_backend.work.domain.Work;
import com.mydea.mydea_backend.work.service.WorkQueryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller slice tests for WorkQueryController.
 * - Security 필터 비활성(addFilters=false): 컨트롤러 HTTP 계약만 검증
 * - Service는 @MockitoBean 으로 스텁
 */
@WebMvcTest(controllers = WorkQueryController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkQueryControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @MockitoBean
    WorkQueryService workQueryService;

    // ---- helpers ----
    private Work makeWork(Long id, Long userId, String name, String previewUrl) {
        return Work.builder()
                .id(id)
                .userId(userId)
                .name(name)
                .workType(Work.WorkType.ring)
                .designType(Work.DesignType.basic)
                .colors(List.of("#ff0000"))
                .flowerPetal("#00ff00")
                .flowerCenter("#0000ff")
                .autoSize(1)
                .radiusMm(new BigDecimal("10.000"))
                .sizeIndex(1)
                .previewUrl(previewUrl)
                .build();
    }

    // ---------- GET /api/works?userId=1&page=0&size=20 ----------
    @Test
    @DisplayName("GET /api/works - 목록 조회: service 결과를 DTO로 매핑하여 200 OK")
    void list_ok() throws Exception {
        Long userId = 1L;
        int page = 0, size = 20;

        // given
        Work w1 = makeWork(100L, userId, "w1", "https://blob/works/100/preview.png");
        Page<Work> stubPage = new PageImpl<>(List.of(w1));
        given(workQueryService.listByUser(eq(userId), eq(page), eq(size))).willReturn(stubPage);
        given(workQueryService.signPreviewUrlOrNull(eq(w1)))
                .willReturn("https://signed.example/works/100/preview.png?sig=abc");

        // when/then
        mvc.perform(get("/api/works")
                        .param("userId", String.valueOf(userId))
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // 응답 본문에 서명 URL 일부가 포함되는지만 느슨하게 확인
                .andExpect(content().string(containsString("signed.example")))
                // Page 직렬화 시 content 배열이 포함되는지 확인 (Spring의 기본 Jackson 직렬화)
                .andExpect(content().string(containsString("content")));

        // 서비스 호출 검증
        then(workQueryService).should().listByUser(eq(userId), eq(page), eq(size));
        then(workQueryService).should().signPreviewUrlOrNull(eq(w1));
        then(workQueryService).shouldHaveNoMoreInteractions();
    }

    // ---------- GET /api/works/{id}?userId=... ----------
    @Test
    @DisplayName("GET /api/works/{id} - 단건 조회: service로 가져오고 서명 URL 포함하여 200 OK")
    void get_ok() throws Exception {
        Long id = 10L;
        Long userId = 1L;

        // given
        Work w = makeWork(id, userId, "target", "https://blob/works/10/preview.png");
        given(workQueryService.getOrThrow(eq(id), eq(userId))).willReturn(w);
        given(workQueryService.signPreviewUrlOrNull(eq(w)))
                .willReturn("https://signed.example/works/10/preview.png?sig=xyz");

        // when/then
        mvc.perform(get("/api/works/{id}", id)
                        .param("userId", String.valueOf(userId))
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // 응답에 서명 URL 일부가 들어있는지만 확인
                .andExpect(content().string(containsString("signed.example")));

        then(workQueryService).should().getOrThrow(eq(id), eq(userId));
        then(workQueryService).should().signPreviewUrlOrNull(eq(w));
        then(workQueryService).shouldHaveNoMoreInteractions();
    }

    @Test
    @DisplayName("GET /api/works/{id} - userId 없이도 조회 가능(서비스는 null로 전달)")
    void get_ok_without_userId() throws Exception {
        Long id = 11L;

        Work w = makeWork(id, 99L, "noUserParam", "https://blob/works/11/preview.png");
        given(workQueryService.getOrThrow(eq(id), isNull())).willReturn(w);
        given(workQueryService.signPreviewUrlOrNull(eq(w)))
                .willReturn("https://signed.example/works/11/preview.png?sig=uvw");

        mvc.perform(get("/api/works/{id}", id).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("signed.example")));

        then(workQueryService).should().getOrThrow(eq(id), isNull());
        then(workQueryService).should().signPreviewUrlOrNull(eq(w));
        then(workQueryService).shouldHaveNoMoreInteractions();
    }
}
