package com.mydea.mydea_backend.order.support;

import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    public Long currentAccountId() {
        // 실제 구현: SecurityContextHolder에서 accountId 추출
        // 예시용: 데모 공용 로그인(계정 ID=1)
        return 1L;
    }
}