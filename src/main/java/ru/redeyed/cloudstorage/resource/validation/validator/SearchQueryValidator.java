package ru.redeyed.cloudstorage.resource.validation.validator;

import jakarta.validation.ConstraintValidatorContext;
import ru.redeyed.cloudstorage.resource.validation.annotation.ValidSearchQuery;
import ru.redeyed.cloudstorage.common.validation.validator.BaseConstraintValidator;
import java.util.regex.Pattern;

public class SearchQueryValidator extends BaseConstraintValidator<ValidSearchQuery, String> {

    private static final String PATTERN = "^[^/\\\\:*?\"<>|]+$";

    private Pattern pattern;

    @Override
    public void initialize(ValidSearchQuery constraintAnnotation) {
        parameterName = constraintAnnotation.parameterName();
        pattern = Pattern.compile(PATTERN);
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String query, ConstraintValidatorContext context) {
        return checkNotBlank(context, query)
                && checkMaxLength(context, query, ResourcePathValidator.RESOURCE_NAME_MAX_LENGTH)
                && checkMaxBytes(context, query, ResourcePathValidator.RESOURCE_NAME_MAX_BYTES)
                && patternMatches(context, query, pattern);
    }
}
