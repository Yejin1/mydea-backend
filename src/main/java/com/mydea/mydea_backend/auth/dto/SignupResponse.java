package com.mydea.mydea_backend.auth.dto;

import java.time.LocalDateTime;

public record SignupResponse(
        Long id,
        String loginId,
        String name,
        String email,
        boolean emailVerified,
        String phone,
        boolean phoneVerified,
        String role,
        String status,
        LocalDateTime createdAt
) {}
