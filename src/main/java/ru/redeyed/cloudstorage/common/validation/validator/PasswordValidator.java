package ru.redeyed.cloudstorage.common.validation.validator;

import jakarta.validation.ConstraintValidatorContext;
import ru.redeyed.cloudstorage.common.validation.annotation.ValidPassword;
import java.util.regex.Pattern;

public class PasswordValidator extends BaseConstraintValidator<ValidPassword, String> {

    private static final int MIN_LENGTH = 5;
    private static final int MAX_LENGTH = 20;

    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9!@#$%^&*(),.?\":{}|<>\\[\\]/`~+=\\-_';]+$");

    @Override
    public void initialize(ValidPassword constraintAnnotation) {
        parameterName = constraintAnnotation.parameterName();
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        return checkNotBlank(context, password)
                && checkLengthBetween(context, password, MIN_LENGTH, MAX_LENGTH)
                && patternMatches(context, password, PATTERN);
    }
}
