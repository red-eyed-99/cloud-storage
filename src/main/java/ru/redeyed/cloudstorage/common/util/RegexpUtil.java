package ru.redeyed.cloudstorage.common.util;

import lombok.experimental.UtilityClass;
import java.util.Set;

@UtilityClass
public class RegexpUtil {

    private static final Set<Character> SPECIAL_CHARACTERS = Set.of(
            '^', '$', '(', ')', '[', ']', '\\', '/', '*', '+', '?', '|', '.'
    );

    public static String escapeSpecialCharacters(String string) {
        var stringBuilder = new StringBuilder();

        for (var i = 0; i < string.length(); i++) {
            if (SPECIAL_CHARACTERS.contains(string.charAt(i))) {
                stringBuilder.append('\\');
            }

            stringBuilder.append(string.charAt(i));
        }

        return stringBuilder.toString();
    }
}
