package ru.redeyed.cloudstorage.s3.minio;

import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.RemoveObjectsArgs;
import io.minio.Result;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import io.minio.messages.DeleteObject;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.s3.BucketName;
import ru.redeyed.cloudstorage.s3.SimpleStorageService;
import ru.redeyed.cloudstorage.s3.StorageObjectInfo;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService implements SimpleStorageService {

    private final MinioClient minioClient;

    private final MinioObjectMapper minioObjectMapper;

    @Override
    public Optional<StorageObjectInfo> findObjectInfo(BucketName bucketName, String path) {
        if (PathUtil.isDirectory(path)) {
            return findDirectoryInfo(bucketName, path);
        }

        return findFileInfo(bucketName, path);
    }

    @Override
    public void deleteObject(BucketName bucketName, String path) {
        if (PathUtil.isDirectory(path)) {
            removeDirectory(bucketName, path);
            return;
        }

        removeFile(bucketName, path);
    }

    @Override
    @SneakyThrows
    public void createDirectory(BucketName bucketName, String path) {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName.getValue())
                .object(path)
                .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                .build());
    }

    @Override
    public boolean objectExists(BucketName bucketName, String path) {
        if (PathUtil.isDirectory(path)) {
            return findDirectoryInfo(bucketName, path).isPresent();
        }

        return findFileInfo(bucketName, path).isPresent();
    }

    @SneakyThrows
    private Optional<StorageObjectInfo> findFileInfo(BucketName bucketName, String path) {
        try {
            var statObjectResponse = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName.getValue())
                    .object(path)
                    .build());

            return Optional.of(minioObjectMapper.toStorageObjectInfo(statObjectResponse));

        } catch (ErrorResponseException exception) {
            var code = exception.errorResponse().code();

            if (code.equals(MinioStatusCode.NO_SUCH_KEY.getValue())) {
                return Optional.empty();
            }

            throw exception;
        }
    }

    private Optional<StorageObjectInfo> findDirectoryInfo(BucketName bucketName, String path) {
        path = trimLastSlash(path);

        var directoryName = PathUtil.extractResourceName(path);

        var resultItems = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName.getValue())
                .prefix(path)
                .build());

        return findItem(resultItems, directoryName, true)
                .map(minioObjectMapper::toStorageObjectInfo);
    }

    @SneakyThrows
    private Optional<Item> findItem(Iterable<Result<Item>> resultItems, String resourceName, boolean isDirectory) {
        for (var resultItem : resultItems) {
            var item = resultItem.get();
            var itemResourceName = PathUtil.extractResourceName(item.objectName());

            if (!itemResourceName.equals(resourceName)) {
                continue;
            }

            if (item.isDir() && isDirectory) {
                return Optional.of(item);
            }

            if (!item.isDir() && !isDirectory) {
                return Optional.of(item);
            }
        }

        return Optional.empty();
    }

    @SneakyThrows
    private void removeFile(BucketName bucketName, String path) {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName.getValue())
                .object(path)
                .build());
    }

    @SneakyThrows
    private void removeDirectory(BucketName bucketName, String path) {
        var deleteObjects = new ArrayList<DeleteObject>();

        var resultItemsToDelete = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName.getValue())
                .prefix(path)
                .recursive(true)
                .build());

        for (var objectItem : resultItemsToDelete) {
            var deleteObjectPath = objectItem.get().objectName();
            deleteObjects.add(new DeleteObject(deleteObjectPath));
        }

        var resultDeleteErrors = minioClient.removeObjects(RemoveObjectsArgs.builder()
                .bucket(bucketName.getValue())
                .objects(deleteObjects)
                .build());

        for (var resultDeleteError : resultDeleteErrors) {
            var deleteError = resultDeleteError.get();
            log.error("Error while deleting {} - {}", deleteError.objectName(), deleteError.message());
        }
    }

    private String trimLastSlash(String path) {
        var beginIndex = 0;
        var endIndex = path.length() - 1;
        return path.substring(beginIndex, endIndex);
    }
}
