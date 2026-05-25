package com.app.demo.validation;

import com.app.demo.service.UserService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentPasswordValidator implements ConstraintValidator<CurrentPassword, String> {

    private final UserService userService;

    @Override
    public boolean isValid(String currentPassword, ConstraintValidatorContext context) {

        if (currentPassword == null || currentPassword.isBlank()) {
            return true;
        }

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return userService.checkCurrentPassword(email, currentPassword);
    }
}
