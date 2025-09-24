package com.mydea.mydea_backend.account.repo;

import com.mydea.mydea_backend.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByLoginId(String loginId);
    Optional<Account> findByEmail(String email);
    Optional<Account> findByLoginId(String loginId);
}