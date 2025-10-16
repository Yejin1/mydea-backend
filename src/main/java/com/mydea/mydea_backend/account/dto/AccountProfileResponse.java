package com.mydea.mydea_backend.account.dto;

import com.mydea.mydea_backend.account.domain.Account;
import com.mydea.mydea_backend.account.domain.AccountStatus;
import com.mydea.mydea_backend.account.domain.Role;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AccountProfileResponse {
    private Long id;
    private String loginId;
    private String email;
    private boolean emailVerified;
    private String name;
    private String nickname;
    private String phone;
    private boolean phoneVerified;
    private Role role;
    private AccountStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AccountProfileResponse from(Account a) {
        return AccountProfileResponse.builder()
                .id(a.getId())
                .loginId(a.getLoginId())
                .email(a.getEmail())
                .emailVerified(a.isEmailVerified())
                .name(a.getName())
                .nickname(a.getNickname())
                .phone(a.getPhone())
                .phoneVerified(a.isPhoneVerified())
                .role(a.getRole())
                .status(a.getStatus())
                .createdAt(a.getCreatedAt())
                .updatedAt(a.getUpdatedAt())
                .build();
    }
}
