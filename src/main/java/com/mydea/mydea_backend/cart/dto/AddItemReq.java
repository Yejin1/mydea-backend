package com.mydea.mydea_backend.cart.dto;


import jakarta.validation.constraints.*;
import org.springframework.lang.Nullable;

public record AddItemReq(
        @NotNull Long workId,
        @NotBlank String optionHash,     // 데모면 "DEFAULT"
        @NotBlank String name,           // 스냅샷(표시용)
        @Nullable String thumbUrl,       // 스냅샷(표시용)
        @PositiveOrZero int unitPrice,   // 데모면 클라 신뢰 가능
        @Min(1) int quantity
) {}