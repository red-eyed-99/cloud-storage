package ru.redeyed.cloudstorage.common.validation.validator;

import jakarta.validation.ConstraintValidatorContext;
import ru.redeyed.cloudstorage.common.validation.annotation.ValidResourcePath;
import ru.redeyed.cloudstorage.resource.ResourceUtil;
import java.util.regex.Pattern;

public class ResourcePathValidator extends BaseConstraintValidator<ValidResourcePath, String> {

    private static final String NOT_START_WITH_MESSAGE_FORMAT =
            "%s must not start with '" + ResourceUtil.PATH_DELIMITER + "' when it's not root directory.";

    private static final int MAX_LENGTH = 3000;

    private static final Pattern PATTERN = Pattern.compile("^(?:/|(?:[^/\\\\:*?\"<>|]+/)+)$");

    @Override
    public void initialize(ValidResourcePath constraintAnnotation) {
        parameterName = constraintAnnotation.parameterName();
        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String path, ConstraintValidatorContext context) {
        if (ResourceUtil.isRootDirectory(path)) {
            return true;
        }

        return checkNotBlank(context, path)
                && checkMaxLength(context, path, MAX_LENGTH)
                && checkNotStartWith(context, path, ResourceUtil.PATH_DELIMITER, getNotStartWithMessage())
                && checkEndWith(context, path, ResourceUtil.PATH_DELIMITER)
                && checkExtraSpaces(context, path)
                && patternMatches(context, path, PATTERN);
    }

    private String getNotStartWithMessage() {
        return NOT_START_WITH_MESSAGE_FORMAT.formatted(parameterName);
    }
}
