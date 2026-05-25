package com.app.demo.enums;

import lombok.Getter;

@Getter
public enum UserTokenType {
    LOGIN(
            "/auth/login?token=",
            "LOGIN",
            1,
            "Login to your account",
            "Click this link to login: "
    ),
    VERIFY(
            "/auth/verify?token=",
            "VERIFY",
            1,
            "Verify your account",
            "Click this link to verify account: "
    );

    private final String path;
    private final String type;
    private final int timeout;
    private final String subject;
    private final String message;

    UserTokenType(String path, String type, int timeout, String subject, String message) {
        this.path = path;
        this.type = type;
        this.timeout = timeout;
        this.subject = subject;
        this.message = message;
    }
}
