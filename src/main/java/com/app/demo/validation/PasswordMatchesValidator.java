package com.app.demo.validation;

import com.app.demo.enums.ErrorMessage;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PasswordMatchesValidator
        implements ConstraintValidator<PasswordMatches, Object> {

    private String firstFieldName;
    private String secondFieldName;

    @Override
    public void initialize(PasswordMatches constraintAnnotation) {
        this.firstFieldName = constraintAnnotation.first();
        this.secondFieldName = constraintAnnotation.second();
    }

    @Override
    public boolean isValid(Object object,
                           ConstraintValidatorContext context) {

        try {
            Object firstValue = new BeanWrapperImpl(object).getPropertyValue(firstFieldName);
            Object secondValue = new BeanWrapperImpl(object).getPropertyValue(secondFieldName);

            if (firstValue == null || secondValue == null) {
                return true;
            }

            boolean isValid = firstValue.equals(secondValue);

            if (!isValid) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(ErrorMessage.PASSWORD_NOT_MATCH.getMessage())
                        .addPropertyNode("confirmPassword")
                        .addConstraintViolation();
            }

            return isValid;
        } catch (Exception e) {
            return false;
        }
    }
}