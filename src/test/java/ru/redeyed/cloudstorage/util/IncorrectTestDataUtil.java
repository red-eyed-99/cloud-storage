package ru.redeyed.cloudstorage.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.junit.jupiter.params.provider.Arguments;

@UtilityClass
public class IncorrectTestDataUtil {

    public static Arguments getNullArguments(Object dto, String fieldName) {
        var description = fieldName + " is null";
        setIncorrectValue(dto, fieldName, null);
        return Arguments.of(dto, description);
    }

    public static Arguments getEmptyArguments(Object dto, String fieldName) {
        var description = fieldName + " is empty";
        setIncorrectValue(dto, fieldName, "");
        return Arguments.of(dto, description);
    }

    public static Arguments getMinLengthArguments(Object dto, String fieldName, String fieldValue, int minLength) {
        var descriptionFormat = "%s is less than min length(%d)";
        setIncorrectValue(dto, fieldName, fieldValue);
        return Arguments.of(dto, descriptionFormat.formatted(fieldName, minLength));
    }

    public static Arguments getMaxLengthArguments(Object dto, String fieldName, String fieldValue, int maxLength) {
        var descriptionFormat = "%s is more than max length(%d)";
        setIncorrectValue(dto, fieldName, fieldValue);
        return Arguments.of(dto, descriptionFormat.formatted(fieldName, maxLength));
    }

    public static Arguments getCyrillicArguments(Object dto, String fieldName, String fieldValue) {
        if (!containsCyrillic(fieldValue)) {
            throw new IllegalArgumentException("fieldValue parameter doesn't contain cyrillic characters");
        }

        var description = fieldName + " contains Cyrillic";
        setIncorrectValue(dto, fieldName, fieldValue);
        return Arguments.of(dto, description);
    }

    public static Arguments getStartsWithUnderscoreArguments(Object dto, String fieldName, String fieldValue) {
        if (!fieldValue.startsWith("_")) {
            throw new IllegalArgumentException("fieldValue parameter doesn't starts with underscore");
        }

        var description = fieldName + " starts with underscore";
        setIncorrectValue(dto, fieldName, fieldValue);
        return Arguments.of(dto, description);
    }

    public static Arguments getEndsWithUnderscoreArguments(Object dto, String fieldName, String fieldValue) {
        if (!fieldValue.endsWith("_")) {
            throw new IllegalArgumentException("fieldValue parameter doesn't ends with underscore");
        }

        var description = fieldName + " ends with underscore";
        setIncorrectValue(dto, fieldName, fieldValue);
        return Arguments.of(dto, description);
    }

    @SneakyThrows
    public static void setIncorrectValue(Object object, String fieldName, Object value) {
        var field = object.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(object, value);
    }

    private static boolean containsCyrillic(String text) {
        for (var character : text.toCharArray()) {
            var unicodeBlock = Character.UnicodeBlock.of(character);

            if (unicodeBlock == Character.UnicodeBlock.CYRILLIC) {
                return true;
            }
        }

        return false;
    }
}
