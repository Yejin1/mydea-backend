package com.mydea.mydea_backend.common.web;

import com.mydea.mydea_backend.auth.service.SignupException;
import com.mydea.mydea_backend.auth.service.SignupError;
import org.springframework.http.*;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SignupException.class)
    public ResponseEntity<?> handleSignup(SignupException e) {
        HttpStatus status = (e.getError() == SignupError.LOGIN_ID_DUPLICATED
                || e.getError() == SignupError.EMAIL_DUPLICATED) ? HttpStatus.CONFLICT : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status).body(Map.of(
                "code", e.getError().name(),
                "message", "Signup failed: " + e.getError().name()
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException e) {
        return ResponseEntity.badRequest().body(Map.of(
                "code", "VALIDATION_ERROR",
                "message", e.getBindingResult().getAllErrors().get(0).getDefaultMessage()
        ));
    }
}
