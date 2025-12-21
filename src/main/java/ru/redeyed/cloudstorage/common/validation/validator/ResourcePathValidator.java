package ru.redeyed.cloudstorage.common.validation.validator;

import jakarta.validation.ConstraintValidatorContext;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.common.validation.annotation.ValidResourcePath;
import java.util.regex.Pattern;

public class ResourcePathValidator extends BaseConstraintValidator<ValidResourcePath, String> {

    public static final int RESOURCE_NAME_MAX_LENGTH = 200;
    public static final int RESOURCE_NAME_MAX_BYTES = 220;

    private static final int PATH_MAX_BYTES = 900;

    private static final String PATTERN = "^(?:[^/\\\\:*?\"<>|]+/?)+$";

    private static final String ONLY_DIRECTORY_PATTERN = "^(?:[^/\\\\:*?\"<>|]+/)+$";

    private Pattern pattern;

    private boolean onlyDirectory;

    @Override
    public void initialize(ValidResourcePath constraintAnnotation) {
        parameterName = constraintAnnotation.parameterName();
        onlyDirectory = constraintAnnotation.onlyDirectory();

        pattern = onlyDirectory
                ? Pattern.compile(ONLY_DIRECTORY_PATTERN)
                : Pattern.compile(PATTERN);

        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String path, ConstraintValidatorContext context) {
        if (onlyDirectory && PathUtil.isRootDirectory(path)) {
            return true;
        }

        var resourceName = PathUtil.extractResourceName(path);

        return checkNotBlank(context, path)
                && checkMaxBytes(context, path, PATH_MAX_BYTES)
                && resourceNameIsValid(context, resourceName)
                && checkExtraSpaces(context, path)
                && checkNotStartWith(path, PathUtil.PATH_DELIMITER, context)
                && (!onlyDirectory || checkEndWith(context, path, PathUtil.PATH_DELIMITER))
                && patternMatches(context, path, pattern);
    }

    private boolean resourceNameIsValid(ConstraintValidatorContext context, String value) {
        if (!ConstraintValidationUtil.checkMaxLength(value, RESOURCE_NAME_MAX_LENGTH)) {
            setCustomMessage(context, "Resource name length must be no more than " + RESOURCE_NAME_MAX_LENGTH + " characters");
            return false;
        }

        if (!ConstraintValidationUtil.checkMaxBytes(value, RESOURCE_NAME_MAX_BYTES)) {
            setCustomMessage(context, "Resource name size must be no more than " + RESOURCE_NAME_MAX_BYTES + " bytes");
            return false;
        }

        return true;
    }
}
