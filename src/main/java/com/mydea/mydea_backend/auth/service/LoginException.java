package com.mydea.mydea_backend.auth.service;

public class LoginException extends RuntimeException {
    private final LoginError error;
    public LoginException(LoginError e){ super(e.name()); this.error=e; }
    public LoginError getError(){ return error; }
}
enum LoginError { INVALID_CREDENTIALS, ACCOUNT_SUSPENDED, ACCOUNT_DELETED }