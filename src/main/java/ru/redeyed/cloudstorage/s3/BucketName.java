package ru.redeyed.cloudstorage.s3;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum BucketName {

    USER_FILES("user-files");

    private final String value;
}
