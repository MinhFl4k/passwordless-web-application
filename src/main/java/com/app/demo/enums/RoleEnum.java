package com.app.demo.enums;

import lombok.Getter;

@Getter
public enum RoleEnum {
    ROLE_ADMIN("ROLE_ADMIN"),
    ROLE_USER("ROLE_USER"),
    ROLE_GUEST("ROLE_GUEST");

    private final String message;

    RoleEnum(String message) {
        this.message = message;
    }
}
