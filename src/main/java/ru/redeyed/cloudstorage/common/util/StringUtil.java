package ru.redeyed.cloudstorage.common.util;

import lombok.experimental.UtilityClass;
import java.util.Map;

@UtilityClass
public class StringUtil {

    public static String removeUnnecessaryCharacters(String string, Map<String, String> regexesReplacements) {
        for (var regexReplacement : regexesReplacements.entrySet()) {
            string = string.replaceAll(regexReplacement.getKey(), regexReplacement.getValue());
        }

        return string;
    }
}
