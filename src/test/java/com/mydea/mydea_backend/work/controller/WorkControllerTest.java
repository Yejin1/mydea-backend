package com.mydea.mydea_backend.work.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.mydea.mydea_backend.work.domain.Work;
import com.mydea.mydea_backend.work.service.WorkService;
import com.mydea.mydea_backend.work.dto.WorkRequest;
import com.mydea.mydea_backend.work.dto.WorkResponse;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = WorkController.class)
class WorkControllerTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @TestConfiguration
    static class TestConfig {
        @Bean
        WorkService workService() {
            return mock(WorkService.class);
        }
    }

    @Autowired
    WorkService workService; // 여기서는 mock 주입됨

    @Test
    void create_returns_200() throws Exception {
        when(workService.create(any())).thenReturn(Work.builder().id(1L).name("ok").build());

        mvc.perform(post("/api/works")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                   {"userId":1,"name":"test","workType":"ring","designType":"basic","colors":["#aaaaaa"],"autoSize":0}
                """))
                .andExpect(status().isOk());
    }
}
