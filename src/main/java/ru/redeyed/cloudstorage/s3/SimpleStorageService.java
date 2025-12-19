package ru.redeyed.cloudstorage.s3;

import java.util.List;
import java.util.Optional;

public interface SimpleStorageService {

    Optional<StorageObjectInfo> findObjectInfo(BucketName bucketName, String path);

    void deleteObject(BucketName bucketName, String path);

    List<StorageObjectInfo> getDirectoryObjectsInfo(BucketName bucketName, String path);

    void createDirectory(BucketName bucketName, String path);

    boolean objectExists(BucketName bucketName, String path);
}
