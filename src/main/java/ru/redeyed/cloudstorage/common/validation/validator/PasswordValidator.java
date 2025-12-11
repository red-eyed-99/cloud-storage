package ru.redeyed.cloudstorage.common.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.redeyed.cloudstorage.common.validation.annotation.ValidPassword;
import java.util.regex.Pattern;

public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private int minLength;

    private int maxLength;

    private Pattern pattern;

    private String message;

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        minLength = constraintAnnotation.minLength();
        maxLength = constraintAnnotation.maxLength();
        pattern = Pattern.compile(constraintAnnotation.pattern());
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            setCustomMessage(context, "Password must not be null or empty");
            return false;
        }

        if (password.length() < minLength || password.length() > maxLength) {
            var message = "Password length must be between %d and %d characters";
            setCustomMessage(context, message.formatted(minLength, maxLength));
            return false;
        }

        if (!pattern.matcher(password).matches()) {
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
