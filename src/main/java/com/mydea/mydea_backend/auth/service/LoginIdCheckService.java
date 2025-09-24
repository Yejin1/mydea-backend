package com.mydea.mydea_backend.auth.service;

import com.mydea.mydea_backend.auth.dto.LoginIdCheckResponse;
import com.mydea.mydea_backend.account.repo.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LoginIdCheckService {

    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public LoginIdCheckResponse check(String loginId) {
        boolean exists = accountRepository.existsByLoginId(loginId);
        return new LoginIdCheckResponse(loginId, !exists);
    }
}
