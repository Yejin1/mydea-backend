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


}