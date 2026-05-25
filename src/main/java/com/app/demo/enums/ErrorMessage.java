package com.app.demo.enums;

import lombok.Getter;

@Getter
public enum ErrorMessage {

    ACCOUNT_LOCKED("Your account is temporarily locked due to multiple failed sign-in attempts."),
    EMAIL_EXIST("Email already exists"),
    EMAIL_REQUIRED("Email is required"),
    EMAIL_NOT_FOUND("Email not found"),
    INVALID_EMAIL_PASSWORD("Invalid email or password"),
    PASSWORD_NOT_MATCH("Password do not match"),
    ROLE_NOT_FOUND("Role not found"),
    TOKEN_NOT_FOUND("Token not found"),
    USER_NOT_FOUND("User not found");

    private final String message;

    ErrorMessage(String message) {
        this.message = message;
    }
}
