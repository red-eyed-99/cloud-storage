package ru.redeyed.cloudstorage.user.validation.validator;

import jakarta.validation.ConstraintValidatorContext;
import ru.redeyed.cloudstorage.common.validation.validator.BaseConstraintValidator;
import ru.redeyed.cloudstorage.user.validation.annotation.ValidUsername;
import java.util.regex.Pattern;

public class UsernameValidator extends BaseConstraintValidator<ValidUsername, String> {

    public static final int MIN_LENGTH = 5;
    public static final int MAX_LENGTH = 20;

    private static final Pattern PATTERN = Pattern.compile("^[a-zA-Z0-9]+[a-zA-Z_0-9]*[a-zA-Z0-9]+$");

    @Override
    public void initialize(ValidUsername constraintAnnotation) {
        parameterName = constraintAnnotation.parameterName();
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String username, ConstraintValidatorContext context) {
        return checkNotBlank(context, username)
                && checkLengthBetween(context, username, MIN_LENGTH, MAX_LENGTH)
                && checkNotStartOrEndWith(context, username, "_")
                && patternMatches(context, username, PATTERN);
    }
}
