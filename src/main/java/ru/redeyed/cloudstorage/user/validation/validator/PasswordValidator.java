package ru.redeyed.cloudstorage.user.validation.validator;

import jakarta.validation.ConstraintValidatorContext;
import ru.redeyed.cloudstorage.common.validation.validator.BaseConstraintValidator;
import ru.redeyed.cloudstorage.user.validation.annotation.ValidPassword;
import java.util.regex.Pattern;

public class PasswordValidator extends BaseConstraintValidator<ValidPassword, String> {

    public static final int MIN_LENGTH = 5;
    public static final int MAX_LENGTH = 20;

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
