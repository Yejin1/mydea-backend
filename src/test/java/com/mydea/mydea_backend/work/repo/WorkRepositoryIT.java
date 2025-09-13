package com.mydea.mydea_backend.work.repo;

import com.mydea.mydea_backend.work.domain.Work;
import com.mydea.mydea_backend.work.domain.Work.DesignType;
import com.mydea.mydea_backend.work.domain.Work.WorkType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.MySQLContainer;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
class WorkRepositoryIT {

    @Container
    static MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("mydea_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL::getUsername);
        registry.add("spring.datasource.password", MYSQL::getPassword);
    }

    @Autowired
    WorkRepository workRepository;

    @Test
    void save_and_find_Work_with_json_colors_and_enums() {
        Work w = Work.builder()
                .userId(1L)
                .name("핑크 플라워 반지")
                .workType(WorkType.ring)
                .designType(DesignType.flower)
                .colors(List.of("#feadad", "#a1d9a4", "#c6cae0"))
                .flowerPetal("#ffb6c1")
                .flowerCenter("#ffe066")
                .autoSize(0)
                .radiusMm(new BigDecimal("8.400"))
                .sizeIndex(2)
                .build();

        Work saved = workRepository.save(w);
        assertThat(saved.getId()).isNotNull();

        Work found = workRepository.findById(saved.getId()).orElseThrow();
        assertThat(found.getWorkType()).isEqualTo(WorkType.ring);
        assertThat(found.getDesignType()).isEqualTo(DesignType.flower);
        assertThat(found.getColors()).containsExactly("#feadad", "#a1d9a4", "#c6cae0");
        assertThat(found.getFlowerPetal()).isEqualTo("#ffb6c1");
        assertThat(found.getFlowerCenter()).isEqualTo("#ffe066");
        assertThat(found.getRadiusMm()).isEqualByComparingTo("8.400");
        assertThat(found.getSizeIndex()).isEqualTo(2);
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }
}
