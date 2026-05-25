package com.app.demo.validation;

import com.app.demo.dto.request.UserUpdateDto;
import com.app.demo.enums.ErrorMessage;
import com.app.demo.repository.UserRepository;
import com.app.demo.service.UserService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UniqueEmailForUpdateValidator
        implements ConstraintValidator<UniqueEmailForUpdate, UserUpdateDto> {

    private final UserRepository userRepository;

    private final UserService userService;

    @Override
    public boolean isValid(UserUpdateDto dto, ConstraintValidatorContext context) {

        if (dto.getEmail() == null || dto.getEmail().isEmpty()) {
            return true;
        }

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        Long loggedInUserId = userService.getLoggedInUserId(authentication);

        if (loggedInUserId  == null) {
            return false;
        }

        boolean isExist = userRepository.existsByEmailAndIdNot(dto.getEmail(), loggedInUserId );

        if (isExist) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(ErrorMessage.EMAIL_EXIST.getMessage())
                    .addPropertyNode("email")
                    .addConstraintViolation();
        }

        return !isExist;
    }
}