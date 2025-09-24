package com.mydea.mydea_backend.auth.dto;
import jakarta.validation.constraints.*;

public record LoginRequest(
        @NotBlank @Size(min=4, max=50) String loginId,
        @NotBlank @Size(min=8, max=100) String password
) {}
