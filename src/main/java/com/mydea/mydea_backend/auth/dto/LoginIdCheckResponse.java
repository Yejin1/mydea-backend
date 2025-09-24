package com.mydea.mydea_backend.auth.dto;

public record LoginIdCheckResponse(
        String loginId,
        boolean available
) {}