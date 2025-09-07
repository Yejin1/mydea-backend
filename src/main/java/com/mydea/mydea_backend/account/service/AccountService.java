package com.mydea.mydea_backend.account.service;

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
public class AccountService {
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Account register(String email, String rawPassword, String name, String phone) {
        if (accountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }
        Account account = Account.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .name(name)
                .phone(phone)
                .role(Role.USER)
                .status(AccountStatus.ACTIVE)
                .build();
        return accountRepository.save(account);
    }
}