package com.mydea.mydea_backend.account.controller;

import com.mydea.mydea_backend.account.dto.AccountProfileResponse;
import com.mydea.mydea_backend.account.dto.UpdateProfileRequest;
import com.mydea.mydea_backend.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {
    private final AccountService accountService;

    @GetMapping("/me")
    public ResponseEntity<AccountProfileResponse> me(Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(accountService.getProfile(userId));
    }

    @PatchMapping("/me")
    public ResponseEntity<AccountProfileResponse> updateMe(Authentication auth,
            @Valid @RequestBody UpdateProfileRequest req) {
        Long userId = Long.valueOf(auth.getName());
        return ResponseEntity.ok(accountService.updateProfile(userId, req));
    }
}
