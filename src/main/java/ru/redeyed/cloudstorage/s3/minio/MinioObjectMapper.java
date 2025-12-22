package ru.redeyed.cloudstorage.s3.minio;

import io.minio.SnowballObject;
import io.minio.StatObjectResponse;
import io.minio.messages.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.resource.ResourcePathUtil;
import ru.redeyed.cloudstorage.s3.StorageObjectInfo;
import java.util.List;

@Mapper
public abstract class MinioObjectMapper {

    @Mapping(target = "path", expression = "java(item.objectName())")
    @Mapping(target = "name", source = "item", qualifiedByName = "getName")
    @Mapping(target = "size", expression = "java(item.size())")
    @Mapping(target = "isDirectory", expression = "java(item.isDir())")
    public abstract StorageObjectInfo toStorageObjectInfo(Item item);

    @Mapping(target = "path", expression = "java(statObjectResponse.object())")
    @Mapping(target = "name", source = "statObjectResponse", qualifiedByName = "getName")
    @Mapping(target = "isDirectory", constant = "false")
    public abstract StorageObjectInfo toStorageObjectInfo(StatObjectResponse statObjectResponse);

    @Mapping(target = "path", source = "snowballObject", qualifiedByName = "getPath")
    @Mapping(target = "name", source = "snowballObject", qualifiedByName = "getName")
    @Mapping(target = "size", expression = "java(snowballObject.size())")
    @Mapping(target = "isDirectory", source = "snowballObject", qualifiedByName = "isDirectory")
    public abstract StorageObjectInfo toStorageObjectInfo(SnowballObject snowballObject);

    public abstract List<StorageObjectInfo> toStorageObjectsInfo(List<SnowballObject> snowballObjects);

    @Named("getName")
    protected String getName(Item item) {
        return PathUtil.extractResourceName(item.objectName());
    }

    @Named("getName")
    protected String getName(StatObjectResponse statObjectResponse) {
        return PathUtil.extractResourceName(statObjectResponse.object());
    }

    @Named("getPath")
    protected String getPath(SnowballObject snowballObject) {
        return ResourcePathUtil.extractResourcePath(snowballObject.name());
    }

    @Named("getName")
    protected String getName(SnowballObject snowballObject) {
        return PathUtil.extractResourceName(snowballObject.name());
    }

    @Named("isDirectory")
    protected boolean isDirectory(SnowballObject snowballObject) {
        return PathUtil.isDirectory(snowballObject.name());
    }
}
