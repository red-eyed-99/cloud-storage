package ru.redeyed.cloudstorage.common.validation.validator;

import lombok.experimental.UtilityClass;
import java.util.regex.Pattern;

@UtilityClass
public class ConstraintValidationUtil {

    private static final String BLANK_MESSAGE_FORMAT = "Parameter '%s' must not be null or empty.";

    private static final String MAX_LENGTH_MESSAGE_FORMAT = "Parameter '%s' length must be no more than %d characters.";
    private static final String MIN_MAX_LENGTH_MESSAGE_FORMAT = "Parameter '%s' length must be between %d and %d characters.";

    private static final String END_WITH_MESSAGE_FORMAT = "Parameter '%s' must end with '%s'.";
    private static final String NOT_START_WITH_MESSAGE_FORMAT = "Parameter '%s' must not start with '%s'.";
    private static final String NOT_START_OR_END_WITH_MESSAGE_FORMAT = "Parameter '%s' must not start or end with '%s'.";

    private static final String EXTRA_SPACES_MESSAGE_FORMAT = "Parameter '%s' contains extra spaces.";

    private static final Pattern EXTRA_SPACES_PATTERN = Pattern.compile("(^\\s)|(\\s$)|(\\s{2,})|(\\s/)");

    public static boolean isBlank(String value) {
        return value == null || value.isEmpty();
    }

    public static boolean checkLength(String value, int minLength, int maxLength) {
        return value.length() >= minLength && value.length() <= maxLength;
    }

    public static boolean checkMaxLength(String value, int maxLength) {
        return value.length() <= maxLength;
    }

    public static boolean isStartWith(String prefix, String value) {
        return value.startsWith(prefix);
    }

    public static boolean isEndWith(String suffix, String value) {
        return value.endsWith(suffix);
    }

    public static boolean isStartOrEndWith(String pattern, String value) {
        return value.startsWith(pattern) || value.endsWith(pattern);
    }

    public static boolean patternMatches(Pattern pattern, String value) {
        return pattern.matcher(value).matches();
    }

    public static boolean hasExtraSpaces(String value) {
        return EXTRA_SPACES_PATTERN.matcher(value).find();
    }

    public static String getIsBlankMessage(String parameterName) {
        return ConstraintValidationUtil.BLANK_MESSAGE_FORMAT.formatted(parameterName);
    }

    public static String getMinMaxLengthMessage(String parameterName, int minLength, int maxLength) {
        return ConstraintValidationUtil.MIN_MAX_LENGTH_MESSAGE_FORMAT.formatted(parameterName, minLength, maxLength);
    }

    public static String getMaxLengthMessage(String parameterName, int maxLength) {
        return ConstraintValidationUtil.MAX_LENGTH_MESSAGE_FORMAT.formatted(parameterName, maxLength);
    }

    public static String getNotStartWithMessage(String parameterName, String value) {
        return ConstraintValidationUtil.NOT_START_WITH_MESSAGE_FORMAT.formatted(parameterName, value);
    }

    public static String getEndWithMessage(String parameterName, String suffix) {
        return ConstraintValidationUtil.END_WITH_MESSAGE_FORMAT.formatted(parameterName, suffix);
    }

    public static String getNotStartOrEndWithMessage(String parameterName, String pattern) {
        return ConstraintValidationUtil.NOT_START_OR_END_WITH_MESSAGE_FORMAT.formatted(parameterName, pattern);
    }

    public static String getExtraSpacesMessage(String parameterName) {
        return ConstraintValidationUtil.EXTRA_SPACES_MESSAGE_FORMAT.formatted(parameterName);
    }
}
