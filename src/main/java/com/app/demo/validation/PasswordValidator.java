package com.app.demo.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PasswordValidator
        implements ConstraintValidator<ValidPassword, String> {

    @Value("${password.valid.regexp}")
    private String passwordRegex;

    @Override
    public boolean isValid(String password,
                           ConstraintValidatorContext context) {

        if (password == null) {
            return false;
        }

        return password.matches(passwordRegex);
    }
}