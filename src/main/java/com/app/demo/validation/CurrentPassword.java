package com.app.demo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = CurrentPasswordValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CurrentPassword {

    String message() default "Current password is incorrect";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
