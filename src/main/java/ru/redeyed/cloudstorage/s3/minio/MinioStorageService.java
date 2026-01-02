package ru.redeyed.cloudstorage.s3.minio;

import io.minio.CopyObjectArgs;
import io.minio.CopySource;
import io.minio.GetObjectArgs;
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
import org.springframework.web.multipart.MultipartFile;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.s3.BucketName;
import ru.redeyed.cloudstorage.s3.SimpleStorageService;
import ru.redeyed.cloudstorage.s3.StorageObjectInfo;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService implements SimpleStorageService {

    private final MinioClient minioClient;

    private final MinioObjectMapper minioObjectMapper;

    @Override
    @SneakyThrows
    public Optional<StorageObjectInfo> findFileInfo(BucketName bucketName, String path) {
        try {
            var statObjectResponse = minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucketName.getValue())
                    .object(path)
                    .build()
            );

            return Optional.of(minioObjectMapper.toStorageObjectInfo(statObjectResponse));

        } catch (ErrorResponseException exception) {
            var code = exception.errorResponse().code();

            if (code.equals(MinioStatusCode.NO_SUCH_KEY.getValue())) {
                return Optional.empty();
            }

            throw exception;
        }
    }

    @Override
    public Optional<StorageObjectInfo> findDirectoryInfo(BucketName bucketName, String path) {
        path = PathUtil.trimLastSlash(path);

        var directoryName = PathUtil.extractResourceName(path);

        var resultItems = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName.getValue())
                .prefix(path)
                .build()
        );

        return findItem(resultItems, directoryName, true)
                .map(minioObjectMapper::toStorageObjectInfo);
    }

    @Override
    @SneakyThrows
    public List<StorageObjectInfo> getDirectoryObjectsInfo(BucketName bucketName, String path, boolean recursive) {
        var storageObjectInfos = new ArrayList<StorageObjectInfo>();

        var resultItems = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName.getValue())
                .prefix(path)
                .recursive(recursive)
                .build()
        );

        for (var resultItem : resultItems) {
            var item = resultItem.get();

            if (item.objectName().equals(path)) {
                continue;
            }

            var storageObjectInfo = minioObjectMapper.toStorageObjectInfo(item);

            storageObjectInfos.add(storageObjectInfo);
        }

        return storageObjectInfos;
    }

    @Override
    @SneakyThrows
    public List<StorageObjectInfo> uploadFiles(BucketName bucketName, String rootPath, List<MultipartFile> files) {
        var uploadedFilesInfo = new ArrayList<StorageObjectInfo>();

        var executor = Executors.newCachedThreadPool();

        try {
            for (var file : files) {
                var filePath = Objects.requireNonNull(file.getOriginalFilename());
                var fullPath = rootPath + filePath;

                executor.submit(() -> minioClient.putObject(PutObjectArgs.builder()
                        .bucket(bucketName.getValue())
                        .object(fullPath)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .build()
                ));

                var fileName = PathUtil.extractResourceName(filePath);

                var uploadedFileInfo = new StorageObjectInfo(fullPath, fileName, file.getSize(), false);

                uploadedFilesInfo.add(uploadedFileInfo);
            }
        } finally {
            executor.shutdown();
        }

        try {
            if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }

        return uploadedFilesInfo;
    }

    @Override
    @SneakyThrows
    public StorageObjectInfo createDirectory(BucketName bucketName, String path) {
        minioClient.putObject(PutObjectArgs.builder()
                .bucket(bucketName.getValue())
                .object(path)
                .stream(new ByteArrayInputStream(new byte[]{}), 0, -1)
                .build()
        );

        var directoryName = PathUtil.extractResourceName(path);

        return new StorageObjectInfo(path, directoryName, 0, true);
    }

    @Override
    @SneakyThrows
    public InputStream downloadFile(BucketName bucketName, String path) {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName.getValue())
                        .object(path)
                        .build()
        );
    }

    @Override
    @SneakyThrows
    public void downloadDirectory(BucketName bucketName, String path, ZipOutputStream zipOutputStream) {
        var resultItemsToDownload = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName.getValue())
                .prefix(path)
                .recursive(true)
                .build()
        );

        for (var resultItem : resultItemsToDownload) {
            var item = resultItem.get();

            var directoryName = PathUtil.extractResourceName(path);
            var entryName = PathUtil.extractPathFromDirectory(directoryName, item.objectName());

            zipOutputStream.putNextEntry(new ZipEntry(entryName));

            try (var inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(BucketName.USER_FILES.getValue())
                            .object(item.objectName())
                            .build())
            ) {
                inputStream.transferTo(zipOutputStream);
            }

            zipOutputStream.closeEntry();
        }
    }

    @Override
    @SneakyThrows
    public void removeFile(BucketName bucketName, String path) {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName.getValue())
                .object(path)
                .build()
        );
    }

    @Override
    @SneakyThrows
    public void removeDirectory(BucketName bucketName, String path) {
        var objectsToDelete = new ArrayList<DeleteObject>();

        var resultItemsToDelete = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName.getValue())
                .prefix(path)
                .recursive(true)
                .build()
        );

        for (var objectItem : resultItemsToDelete) {
            var deleteObjectPath = objectItem.get().objectName();
            objectsToDelete.add(new DeleteObject(deleteObjectPath));
        }

        if (objectsToDelete.isEmpty()) {
            return;
        }

        var resultDeleteErrors = minioClient.removeObjects(RemoveObjectsArgs.builder()
                .bucket(bucketName.getValue())
                .objects(objectsToDelete)
                .build()
        );

        for (var resultDeleteError : resultDeleteErrors) {
            var deleteError = resultDeleteError.get();
            log.error("Error while deleting {} - {}", deleteError.objectName(), deleteError.message());
        }
    }

    @Override
    public boolean fileExists(BucketName bucketName, String path) {
        return findFileInfo(bucketName, path).isPresent();
    }

    @Override
    public boolean directoryExists(BucketName bucketName, String path) {
        return findDirectoryInfo(bucketName, path).isPresent();
    }

    @Override
    @SneakyThrows
    public void moveFile(BucketName bucketName, String oldPath, String newPath) {
        minioClient.copyObject(CopyObjectArgs.builder()
                .bucket(bucketName.getValue())
                .object(newPath)
                .source(CopySource.builder()
                        .bucket(bucketName.getValue())
                        .object(oldPath)
                        .build())
                .build()
        );

        removeFile(bucketName, oldPath);
    }

    @Override
    @SneakyThrows
    public void moveDirectory(BucketName bucketName, String oldPath, String newPath) {
        var directoryName = PathUtil.extractResourceName(oldPath);

        var directoryObjectsInfo = getDirectoryObjectsInfo(bucketName, oldPath, true);

        for (var objectInfo : directoryObjectsInfo) {
            var pathFromDirectory = PathUtil.extractPathFromDirectory(directoryName, objectInfo.path());
            var pathWithoutParentDirectory = PathUtil.removeRootParentDirectory(pathFromDirectory);

            minioClient.copyObject(CopyObjectArgs.builder()
                    .bucket(bucketName.getValue())
                    .object(newPath + pathWithoutParentDirectory)
                    .source(CopySource.builder()
                            .bucket(bucketName.getValue())
                            .object(objectInfo.path())
                            .build())
                    .build()
            );
        }

        removeDirectory(bucketName, oldPath);
        createDirectory(bucketName, newPath);
    }

    @Override
    @SneakyThrows
    public List<StorageObjectInfo> search(BucketName bucketName, String path, String pattern) {
        var foundObjectsInfo = new ArrayList<StorageObjectInfo>();

        var resultItems = minioClient.listObjects(ListObjectsArgs.builder()
                .bucket(bucketName.getValue())
                .prefix(path)
                .recursive(true)
                .build()
        );

        for (var resultItem : resultItems) {
            var item = resultItem.get();

            var resourceName = PathUtil.extractResourceName(item.objectName());

            if (resourceName.matches(pattern)) {
                foundObjectsInfo.add(minioObjectMapper.toStorageObjectInfo(item));
            }
        }

        return foundObjectsInfo;
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
}
