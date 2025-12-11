package ru.redeyed.cloudstorage.common.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.redeyed.cloudstorage.common.validation.annotation.ValidUsername;
import java.util.regex.Pattern;

public class UsernameValidator implements ConstraintValidator<ValidUsername, String> {

    private int minLength;

    private int maxLength;

    private Pattern pattern;

    private String message;

    @Override
    public void initialize(ValidUsername constraintAnnotation) {
        minLength = constraintAnnotation.minLength();
        maxLength = constraintAnnotation.maxLength();
        pattern = Pattern.compile(constraintAnnotation.pattern());
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        if (username == null || username.isBlank()) {
            setCustomMessage(context, "Username must not be null or empty.");
            return false;
        }

        if (username.length() < minLength || username.length() > maxLength) {
            var message = "Username length must be between %d and %d characters.";
            setCustomMessage(context, message.formatted(minLength, maxLength));
            return false;
        }

        if (!pattern.matcher(username).matches()) {
            setCustomMessage(context, message);
            return false;
        }

        return true;
    }

    private void setCustomMessage(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
