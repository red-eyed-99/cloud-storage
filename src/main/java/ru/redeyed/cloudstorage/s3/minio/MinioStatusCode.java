package ru.redeyed.cloudstorage.s3.minio;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum MinioStatusCode {

    NO_SUCH_KEY("NoSuchKey");

    private final String value;
}
