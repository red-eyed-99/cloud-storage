package ru.redeyed.cloudstorage.common.validation.validator;

import jakarta.validation.ConstraintValidatorContext;
import ru.redeyed.cloudstorage.common.validation.annotation.ValidResourcePathParam;
import ru.redeyed.cloudstorage.resource.ResourceUtil;
import java.util.regex.Pattern;

public class ResourcePathParamValidator extends BaseConstraintValidator<ValidResourcePathParam, String> {

    private static final int MAX_LENGTH = 3000;

    private static final String PATTERN = "^(?:[^/\\\\:*?\"<>|]+/?)+$";

    private static final String ONLY_DIRECTORY_PATTERN = "^(?:[^/\\\\:*?\"<>|]+/)+$";

    private Pattern pattern;

    private boolean onlyDirectory;

    @Override
    public void initialize(ValidResourcePathParam constraintAnnotation) {
        parameterName = constraintAnnotation.parameterName();
        onlyDirectory = constraintAnnotation.onlyDirectory();

        pattern = onlyDirectory
                ? Pattern.compile(ONLY_DIRECTORY_PATTERN)
                : Pattern.compile(PATTERN);

        message = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(String path, ConstraintValidatorContext context) {
        if (onlyDirectory && ResourceUtil.isRootDirectory(path)) {
            return true;
        }

        var resourceName = ResourceUtil.extractResourceName(path);

        return checkNotBlank(context, path)
                && checkMaxLength(context, path, MAX_LENGTH)
                && checkResourceNameMaxLength(context, resourceName)
                && checkExtraSpaces(context, path)
                && checkNotStartWith(path, ResourceUtil.PATH_DELIMITER, context)
                && (!onlyDirectory || checkEndWith(context, path, ResourceUtil.PATH_DELIMITER))
                && patternMatches(context, path, pattern);
    }

    private boolean checkResourceNameMaxLength(ConstraintValidatorContext context, String value) {
        var maxLength = ResourceNameValidator.MAX_LENGTH;

        if (!ConstraintValidationUtil.checkMaxLength(value, maxLength)) {
            setCustomMessage(context, "Resource name must be no more than " + maxLength + " characters");
            return false;
        }

        return true;
    }
}
