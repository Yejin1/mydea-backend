package com.mydea.mydea_backend.auth.service;

import com.mydea.mydea_backend.auth.dto.*;
import com.mydea.mydea_backend.account.domain.*;
import com.mydea.mydea_backend.account.repo.AccountRepository;
import com.mydea.mydea_backend.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @RequiredArgsConstructor
public class LoginService {

    private final AccountRepository accounts;
    private final PasswordEncoder encoder;
    private final JwtTokenProvider jwt;

    @Transactional
    public LoginResponse login(LoginRequest req) {
        Account acc = accounts.findByLoginId(req.loginId())
                .orElseThrow(() -> new LoginException(LoginError.INVALID_CREDENTIALS));

        if (!encoder.matches(req.password(), acc.getPassword()))
            throw new LoginException(LoginError.INVALID_CREDENTIALS);

        if (acc.getStatus() == AccountStatus.SUSPENDED)
            throw new LoginException(LoginError.ACCOUNT_SUSPENDED);
        if (acc.getStatus() == AccountStatus.DELETED)
            throw new LoginException(LoginError.ACCOUNT_DELETED);

        acc.touchLastLoginAt(); // last_login_at 갱신 (엔티티에 추가해둔 메서드)

        String token = jwt.createAccessToken(acc.getId(), acc.getLoginId(), acc.getRole().name());
        return new LoginResponse(
                token, "Bearer", jwt.getAccessTtlSeconds(),
                new LoginResponse.AccountBrief(acc.getId(), acc.getLoginId(), acc.getName(),
                        acc.getRole().name(), acc.getStatus().name())
        );
    }
}
