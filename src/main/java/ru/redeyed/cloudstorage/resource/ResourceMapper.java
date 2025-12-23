package ru.redeyed.cloudstorage.resource;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.redeyed.cloudstorage.resource.dto.ResourceResponseDto;
import ru.redeyed.cloudstorage.s3.StorageObjectInfo;
import java.util.List;

@Mapper
public abstract class ResourceMapper {

    @Mapping(target = "path", source = "objectInfo", qualifiedByName = "getPath")
    @Mapping(target = "size", source = "objectInfo", qualifiedByName = "getSize")
    @Mapping(target = "type", source = "objectInfo", qualifiedByName = "getType")
    public abstract ResourceResponseDto toResourceResponseDto(StorageObjectInfo objectInfo);

    public abstract List<ResourceResponseDto> toResourceResponseDtos(List<StorageObjectInfo> objectInfos);

    @Named("getPath")
    protected String getPath(StorageObjectInfo objectInfo) {
        var path = objectInfo.path();

        if (ResourcePathUtil.hasUserFolder(path)) {
            return ResourcePathUtil.extractResourcePath(path);
        }

        return path;
    }

    @Named("getSize")
    protected Long getSize(StorageObjectInfo objectInfo) {
        return objectInfo.isDirectory() ? null : objectInfo.size();
    }

    @Named("getType")
    protected ResourceType getType(StorageObjectInfo objectInfo) {
        return objectInfo.isDirectory()
                ? ResourceType.DIRECTORY
                : ResourceType.FILE;
    }
}
