package com.mydea.mydea_backend.auth.controller;

import com.mydea.mydea_backend.auth.dto.*;
import com.mydea.mydea_backend.auth.service.LoginIdCheckService;
import com.mydea.mydea_backend.auth.service.LoginService;
import com.mydea.mydea_backend.auth.service.SignupService;
import com.mydea.mydea_backend.auth.service.PasswordChangeService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final SignupService signupService;
    private final LoginIdCheckService loginIdCheckService;
    private final LoginService loginService;
    private final PasswordChangeService passwordChangeService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@Valid @RequestBody SignupRequest req) {
        SignupResponse res = signupService.signup(req);
        return ResponseEntity.status(201).body(res);
    }

    @GetMapping("/check-login-id")
    public ResponseEntity<LoginIdCheckResponse> checkLoginId(
            @RequestParam @Pattern(regexp = "^[a-zA-Z0-9._-]{4,50}$", message = "loginId는 4~50자의 영문/숫자/._- 만 허용됩니다.") String loginId) {
        return ResponseEntity.ok(loginIdCheckService.check(loginId));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(loginService.login(req));
    }

    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(org.springframework.security.core.Authentication auth,
            @Valid @RequestBody ChangePasswordRequest req) {
        Long userId = Long.valueOf(auth.getName());
        passwordChangeService.change(userId, req);
        return ResponseEntity.noContent().build();
    }
}
