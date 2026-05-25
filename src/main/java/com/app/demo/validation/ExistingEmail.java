package com.app.demo.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ExistingEmailValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ExistingEmail {
    String message() default "Email does not exist";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
