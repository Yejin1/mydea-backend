package com.mydea.mydea_backend.auth.service;

public class SignupException extends RuntimeException {
    private final SignupError error;
    public SignupException(SignupError error) { super(error.name()); this.error = error; }
    public SignupError getError() { return error; }
}
