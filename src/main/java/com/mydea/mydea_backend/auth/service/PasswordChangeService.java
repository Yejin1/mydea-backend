package com.mydea.mydea_backend.auth.service;

import com.mydea.mydea_backend.auth.dto.ChangePasswordRequest;
import com.mydea.mydea_backend.account.domain.Account;
import com.mydea.mydea_backend.account.repo.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasswordChangeService {
    private final AccountRepository accounts;
    private final PasswordEncoder encoder;

    @Transactional
    public void change(Long accountId, ChangePasswordRequest req) {
        Account acc = accounts.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!encoder.matches(req.currentPassword(), acc.getPassword())) {
            // 현재 비밀번호 불일치
            throw new LoginException(LoginError.INVALID_CREDENTIALS);
        }

        acc.setPassword(encoder.encode(req.newPassword()));
        accounts.save(acc);
    }
}
