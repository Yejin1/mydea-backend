package com.mydea.mydea_backend.account.service;

import com.mydea.mydea_backend.account.domain.Account;
import com.mydea.mydea_backend.account.dto.AccountProfileResponse;
import com.mydea.mydea_backend.account.dto.UpdateProfileRequest;
import com.mydea.mydea_backend.account.repo.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public AccountProfileResponse getProfile(Long accountId) {
        Account a = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        return AccountProfileResponse.from(a);
    }

    @Transactional
    public AccountProfileResponse updateProfile(Long accountId, UpdateProfileRequest req) {
        Account a = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (req.getName() != null)
            a.setName(req.getName());
        if (req.getNickname() != null)
            a.setNickname(req.getNickname());
        if (req.getPhone() != null)
            a.setPhone(req.getPhone());

        Account saved = accountRepository.save(a);
        return AccountProfileResponse.from(saved);
    }
}