package ru.redeyed.cloudstorage.s3;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.zip.ZipOutputStream;

public interface SimpleStorageService {

    Optional<StorageObjectInfo> findFileInfo(BucketName bucketName, String path);

    Optional<StorageObjectInfo> findDirectoryInfo(BucketName bucketName, String path);

    List<StorageObjectInfo> getDirectoryObjectsInfo(BucketName bucketName, String path);

    void createDirectory(BucketName bucketName, String path);

    InputStream downloadFile(BucketName bucketName, String path);

    void downloadDirectory(BucketName bucketName, String path, ZipOutputStream zipOutputStream);

    void removeFile(BucketName bucketName, String path);

    void removeDirectory(BucketName bucketName, String path);

    boolean fileExists(BucketName bucketName, String path);

    boolean directoryExists(BucketName bucketName, String path);
}
