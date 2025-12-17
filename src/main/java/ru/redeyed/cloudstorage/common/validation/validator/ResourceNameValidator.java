package ru.redeyed.cloudstorage.common.validation.validator;

import jakarta.validation.ConstraintValidatorContext;
import ru.redeyed.cloudstorage.common.validation.annotation.ValidResourceName;
import java.util.regex.Pattern;

public class ResourceNameValidator extends BaseConstraintValidator<ValidResourceName, String> {

    public static final int MAX_LENGTH = 200;

    private static final Pattern PATTERN = Pattern.compile("^[^/\\\\:*?\"<>|]+$");

    @Override
    public void initialize(ValidResourceName constraintAnnotation) {
        parameterName = constraintAnnotation.parameterName();
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String name, ConstraintValidatorContext context) {
        return checkNotBlank(context, name)
                && checkMaxLength(context, name, MAX_LENGTH)
                && checkExtraSpaces(context, name)
                && patternMatches(context, name, PATTERN);
    }
}
