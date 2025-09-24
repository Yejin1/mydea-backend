package com.mydea.mydea_backend.auth.dto;

import jakarta.validation.constraints.*;

public record SignupRequest(
        @NotBlank @Size(min=4, max=50) @Pattern(regexp = "^[a-zA-Z0-9._-]+$")
        String loginId,
        @NotBlank @Size(min=8, max=100)
        String password,
        @Size(max=100) String name,
        @Email @Size(max=255) String email,
        @Size(max=30) String phone
) {}
