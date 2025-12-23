package ru.redeyed.cloudstorage.common.util;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum DataUnit {

    BYTE("B"),
    KILOBYTE("KB"),
    MEGABYTE("MB"),
    GIGABYTE("GB"),
    TERABYTE("TB");

    private final String suffix;
}
