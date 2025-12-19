package ru.redeyed.cloudstorage.s3.minio;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.redeyed.cloudstorage.s3.BucketName;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinioInitializer {

    private final MinioClient minioClient;

    @PostConstruct
    public void init() {
        initBuckets();
    }

    @SneakyThrows
    private void initBuckets() {
        var createdBuckets = new ArrayList<String>();

        for (var bucketName : BucketName.values()) {
            var bucketExists = minioClient.bucketExists(getBucketExistsArgs(bucketName));

            if (!bucketExists) {
                minioClient.makeBucket(getMakeBucketArgs(bucketName));
                createdBuckets.add(bucketName.getValue());
            }
        }

        if (createdBuckets.isEmpty()) {
            log.info("All buckets are initialized");
        } else {
            log.info("The following buckets were created: {}", String.join(",", createdBuckets));
        }
    }

    private BucketExistsArgs getBucketExistsArgs(BucketName bucketName) {
        return BucketExistsArgs.builder()
                .bucket(bucketName.getValue())
                .build();
    }

    private MakeBucketArgs getMakeBucketArgs(BucketName bucketName) {
        return MakeBucketArgs.builder()
                .bucket(bucketName.getValue())
                .build();
    }
}
