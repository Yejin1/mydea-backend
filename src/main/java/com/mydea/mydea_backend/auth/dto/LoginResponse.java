package com.mydea.mydea_backend.auth.dto;

public record LoginResponse(
        String accessToken, String tokenType, long expiresIn,
        AccountBrief account
) {
    public record AccountBrief(Long id, String loginId, String name, String role, String status) {}
}
