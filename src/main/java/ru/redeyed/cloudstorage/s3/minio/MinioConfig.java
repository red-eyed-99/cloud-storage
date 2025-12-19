package ru.redeyed.cloudstorage.s3.minio;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class MinioConfig {

    private final MinioConfigProperties minioConfigProperties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioConfigProperties.url())
                .credentials(minioConfigProperties.accessKey(), minioConfigProperties.secretKey())
                .build();
    }
}
