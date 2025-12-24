package ru.redeyed.cloudstorage.common.validation;

import lombok.experimental.UtilityClass;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;

@UtilityClass
public class ValidationUtil {

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

    public static boolean checkMaxBytes(String value, int maxBytes) {
        var length = value.getBytes(StandardCharsets.UTF_8).length;
        return length <= maxBytes;
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
}
