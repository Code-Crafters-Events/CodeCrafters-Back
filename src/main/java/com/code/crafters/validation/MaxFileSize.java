package com.code.crafters.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = MaxFileSizeValidator.class)
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface MaxFileSize {

    String message() default "La imagen no puede superar los {maxMB}MB";

    long maxMB() default 5;

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}