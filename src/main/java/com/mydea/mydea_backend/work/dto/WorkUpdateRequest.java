package com.mydea.mydea_backend.work.dto;

import com.mydea.mydea_backend.work.domain.Work;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record WorkUpdateRequest(
        @Size(max = 200) String name,
        @NotNull Work.WorkType workType,
        @NotNull Work.DesignType designType,
        @NotNull @Size(min = 1) List<@Pattern(regexp = "^#[0-9a-fA-F]{6}$") String> colors,
        @Pattern(regexp = "^#[0-9a-fA-F]{6}$") String flowerPetal,
        @Pattern(regexp = "^#[0-9a-fA-F]{6}$") String flowerCenter,
        @NotNull @Min(0) Integer autoSize,
        @Digits(integer = 5, fraction = 3) BigDecimal radiusMm,
        Integer sizeIndex
) {}