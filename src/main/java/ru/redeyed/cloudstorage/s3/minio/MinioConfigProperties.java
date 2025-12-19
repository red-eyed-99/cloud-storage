package ru.redeyed.cloudstorage.s3.minio;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "minio")
public record MinioConfigProperties(String url, String accessKey, String secretKey) {
}
