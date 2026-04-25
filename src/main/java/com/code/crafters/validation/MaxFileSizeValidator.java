package com.code.crafters.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Base64;

public class MaxFileSizeValidator implements ConstraintValidator<MaxFileSize, String> {

    private long maxBytes;
    private long maxMB;

    @Override
    public void initialize(MaxFileSize constraintAnnotation) {
        this.maxMB = constraintAnnotation.maxMB();
        this.maxBytes = maxMB * 1024 * 1024;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank())
            return true;

        try {
            String base64Data = value.contains(",") ? value.split(",")[1] : value;

            byte[] decoded = Base64.getDecoder().decode(base64Data);

            if (decoded.length > maxBytes) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate(
                        "La imagen no puede superar los " + maxMB + "MB").addConstraintViolation();
                return false;
            }

            return true;

        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}