package com.app.demo.enums;

import lombok.Getter;

@Getter
public enum SessionKey {
    SESSION_SECURITY_SNAPSHOT("SESSION_SECURITY_SNAPSHOT");

    private final String message;

    SessionKey(String message) {
        this.message = message;
    }
}
