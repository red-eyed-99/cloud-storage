package ru.redeyed.cloudstorage.s3;

public record StorageObjectInfo(String path, String name, long size, boolean isDirectory) {
}
