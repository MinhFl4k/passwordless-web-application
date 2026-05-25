package com.app.demo.enums;

import lombok.Getter;

@Getter
public enum SecurityEventContext {
    CHANGE_EMAIL("You just change your email. Was this you?"),
    CHANGE_PASSWORD("You just change your password. Was this you?"),
    DELETE_PASSKEY("You just delete a passkey. Was this you?");

    private final String message;

    SecurityEventContext(String message) {
        this.message = message;
    }
}
