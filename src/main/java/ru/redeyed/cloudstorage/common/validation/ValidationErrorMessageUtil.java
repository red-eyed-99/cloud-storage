package ru.redeyed.cloudstorage.common.validation;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ValidationErrorMessageUtil {

    private static final String BLANK_MESSAGE_FORMAT = "Parameter '%s' must not be null or empty.";

    private static final String MAX_LENGTH_MESSAGE_FORMAT = "Parameter '%s' length must be no more than %d characters.";
    private static final String MIN_MAX_LENGTH_MESSAGE_FORMAT = "Parameter '%s' length must be between %d and %d characters.";

    private static final String MAX_BYTES_MESSAGE_FORMAT = "Parameter '%s' size must be no more than %d bytes.";

    private static final String END_WITH_MESSAGE_FORMAT = "Parameter '%s' must end with '%s'.";
    private static final String NOT_START_WITH_MESSAGE_FORMAT = "Parameter '%s' must not start with '%s'.";
    private static final String NOT_START_OR_END_WITH_MESSAGE_FORMAT = "Parameter '%s' must not start or end with '%s'.";

    private static final String EXTRA_SPACES_MESSAGE_FORMAT = "Parameter '%s' contains extra spaces.";

    public static String getIsBlankMessage(String parameterName) {
        return BLANK_MESSAGE_FORMAT.formatted(parameterName);
    }

    public static String getMinMaxLengthMessage(String parameterName, int minLength, int maxLength) {
        return MIN_MAX_LENGTH_MESSAGE_FORMAT.formatted(parameterName, minLength, maxLength);
    }

    public static String getMaxLengthMessage(String parameterName, int maxLength) {
        return MAX_LENGTH_MESSAGE_FORMAT.formatted(parameterName, maxLength);
    }

    public static String getMaxBytesMessage(String parameterName, int maxBytes) {
        return MAX_BYTES_MESSAGE_FORMAT.formatted(parameterName, maxBytes);
    }

    public static String getNotStartWithMessage(String parameterName, String value) {
        return NOT_START_WITH_MESSAGE_FORMAT.formatted(parameterName, value);
    }

    public static String getEndWithMessage(String parameterName, String suffix) {
        return END_WITH_MESSAGE_FORMAT.formatted(parameterName, suffix);
    }

    public static String getNotStartOrEndWithMessage(String parameterName, String pattern) {
        return NOT_START_OR_END_WITH_MESSAGE_FORMAT.formatted(parameterName, pattern);
    }

    public static String getExtraSpacesMessage(String parameterName) {
        return EXTRA_SPACES_MESSAGE_FORMAT.formatted(parameterName);
    }
}
