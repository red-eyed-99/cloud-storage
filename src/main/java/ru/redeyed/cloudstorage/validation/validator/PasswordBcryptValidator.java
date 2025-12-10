package ru.redeyed.cloudstorage.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.redeyed.cloudstorage.validation.annotation.PasswordBcryptEncoded;
import java.util.regex.Pattern;

public class PasswordBcryptValidator implements ConstraintValidator<PasswordBcryptEncoded, String> {

    private Pattern pattern;

    private String message;

    @Override
    public void initialize(PasswordBcryptEncoded constraintAnnotation) {
        pattern = Pattern.compile(constraintAnnotation.pattern());
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            setCustomMessage(context, "Password must not be null or empty.");
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
