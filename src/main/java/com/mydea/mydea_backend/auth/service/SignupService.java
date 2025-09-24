// SignupService.java
package com.mydea.mydea_backend.auth.service;

import com.mydea.mydea_backend.auth.dto.SignupRequest;
import com.mydea.mydea_backend.auth.dto.SignupResponse;
import com.mydea.mydea_backend.account.domain.Account;
import com.mydea.mydea_backend.account.domain.AccountStatus;
import com.mydea.mydea_backend.account.domain.Role;
import com.mydea.mydea_backend.account.repo.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SignupService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SignupResponse signup(SignupRequest req) {
        // loginId 중복 체크
        if (accountRepository.existsByLoginId(req.loginId())) {
            throw new SignupException(SignupError.LOGIN_ID_DUPLICATED);
        }
        // email이 있으면 중복 체크(운영전략 따라 선택)
        if (req.email() != null && !req.email().isBlank()
                && accountRepository.findByEmail(req.email()).isPresent()) {
            throw new SignupException(SignupError.EMAIL_DUPLICATED);
        }

        Account saved = accountRepository.save(Account.builder()
                .loginId(req.loginId())
                .password(passwordEncoder.encode(req.password()))
                .name(req.name())
                .email(req.email())
                .phone(req.phone())
                .emailVerified(false)
                .phoneVerified(false)
                .role(Role.USER)
                .status(AccountStatus.ACTIVE)
                .build());

        return new SignupResponse(
                saved.getId(),
                saved.getLoginId(),
                saved.getName(),
                saved.getEmail(),
                saved.isEmailVerified(),
                saved.getPhone(),
                saved.isPhoneVerified(),
                saved.getRole().name(),
                saved.getStatus().name(),
                saved.getCreatedAt()
        );
    }
}
