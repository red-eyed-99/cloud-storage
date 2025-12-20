package ru.redeyed.cloudstorage.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ContentDispositionType {

    ATTACHMENT("attachment");

    private final String value;
}
