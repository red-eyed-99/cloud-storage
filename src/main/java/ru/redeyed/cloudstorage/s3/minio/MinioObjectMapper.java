package ru.redeyed.cloudstorage.s3.minio;

import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.s3.StorageObjectInfo;

@Mapper
public abstract class MinioObjectMapper {

    @Mapping(target = "path", expression = "java(item.objectName())")
    @Mapping(target = "name", source = "item", qualifiedByName = "getName")
    @Mapping(target = "size", expression = "java(item.size())")
    @Mapping(target = "isDirectory", expression = "java(item.isDir())")
    public abstract StorageObjectInfo toStorageObjectInfo(Item item);

    @Mapping(target = "path", expression = "java(statObjectResponse.object())")
    @Mapping(target = "name", source = "statObjectResponse", qualifiedByName = "getName")
    @Mapping(target = "size", expression = "java(statObjectResponse.size())")
    @Mapping(target = "isDirectory", source = "statObjectResponse", qualifiedByName = "isDirectory")
    public abstract StorageObjectInfo toStorageObjectInfo(StatObjectResponse statObjectResponse);

    @Named("getName")
    protected String getName(Item item) {
        return PathUtil.extractResourceName(item.objectName());
    }

    @Named("getName")
    protected String getName(StatObjectResponse statObjectResponse) {
        return PathUtil.extractResourceName(statObjectResponse.object());
    }

    @Named("isDirectory")
    protected boolean isDirectory(StatObjectResponse statObjectResponse) {
        return PathUtil.isDirectory(statObjectResponse.object());
    }
}
