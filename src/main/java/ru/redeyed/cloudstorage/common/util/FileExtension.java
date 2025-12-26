package ru.redeyed.cloudstorage.common.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum FileExtension {

    UNDEFINED(""),
    TXT("txt");

    private final String value;

    public static FileExtension fromString(String value) {
        for (var fileExtension : FileExtension.values()) {
            if (fileExtension.value.equals(value)) {
                return fileExtension;
            }
        }

        throw new IllegalArgumentException("Unknown file extension: " + value);
    }
}
