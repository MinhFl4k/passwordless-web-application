package com.app.demo.validation;

import com.app.demo.repository.UserRepository;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExistingEmailValidator implements ConstraintValidator<ExistingEmail, String> {
    private final UserRepository userRepository;

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context) {
        if (email == null || email.trim().isEmpty()) {
            return true;
        }

        String normalizedEmail = email.trim().toLowerCase();
        return userRepository.existsByEmail(normalizedEmail);
    }
}
