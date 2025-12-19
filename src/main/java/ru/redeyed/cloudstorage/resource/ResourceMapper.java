package ru.redeyed.cloudstorage.resource;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.redeyed.cloudstorage.s3.StorageObjectInfo;

@Mapper
public abstract class ResourceMapper {

    @Mapping(target = "path", source = "objectInfo", qualifiedByName = "getPath")
    @Mapping(target = "size", source = "objectInfo", qualifiedByName = "getSize")
    @Mapping(target = "type", source = "objectInfo", qualifiedByName = "getType")
    public abstract ResourceResponseDto toResourceResponseDto(StorageObjectInfo objectInfo);

    @Named("getPath")
    protected String getPath(StorageObjectInfo objectInfo) {
        return ResourcePathUtil.extractResourcePath(objectInfo.path());
    }

    @Named("getSize")
    protected Long getSize(StorageObjectInfo objectInfo) {
        return objectInfo.isDirectory() ? null : objectInfo.size();
    }

    @Named("getType")
    protected ResourceType getType(StorageObjectInfo objectInfo) {
        return ResourcePathUtil.determineResourceType(objectInfo.path());
    }
}
