package com.mydea.mydea_backend.account.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(max = 100)
    private String name;

    @Size(max = 50)
    private String nickname;

    // 휴대폰번호 패턴
    @Size(max = 30)
    @Pattern(regexp = "^[0-9\\-]+$", message = "숫자와 하이픈만 허용합니다.")
    private String phone;
}
