package com.app.demo.enums;

import lombok.Getter;

@Getter
public enum OtpStatus {
    VALID("Valid OTP"),
    INVALID("Invalid OTP"),
    EXPIRED("OTP has expired"),
    USED("OTP has already been used"),
    NOT_FOUND("OTP not found");

    private final String message;

    OtpStatus(String message) {
        this.message = message;
    }

}
