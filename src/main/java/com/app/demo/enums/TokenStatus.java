package com.app.demo.enums;

import lombok.Getter;

@Getter
public enum TokenStatus {
    INVALID("Invalid Token"),
    EXPIRED("Token has expired"),
    NOT_FOUND("Token not found");

    private final String message;

    TokenStatus(String message) {
        this.message = message;
    }
}
