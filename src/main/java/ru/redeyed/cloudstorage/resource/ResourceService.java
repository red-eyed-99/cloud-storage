package ru.redeyed.cloudstorage.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.s3.BucketName;
import ru.redeyed.cloudstorage.s3.SimpleStorageService;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final SimpleStorageService storageService;

    private final ResourceMapper resourceMapper;

    public ResourceResponseDto getResource(UUID userId, String path) {
        var resourcePath = ResourcePathUtil.createUserResourcePath(userId, path);

        var storageObjectInfo = storageService.findObjectInfo(BucketName.USER_FILES, resourcePath)
                .orElseThrow(ResourceNotFoundException::new);

        return resourceMapper.toResourceResponseDto(storageObjectInfo);
    }

    public void deleteResource(UUID userId, String path) {
        var resourcePath = ResourcePathUtil.createUserResourcePath(userId, path);
        storageService.deleteObject(BucketName.USER_FILES, resourcePath);
    }

    public ResourceResponseDto createDirectory(UUID userId, String path) {
        var resourcePath = ResourcePathUtil.createUserResourcePath(userId, path);

        if (storageService.objectExists(BucketName.USER_FILES, resourcePath)) {
            throw new ResourceAlreadyExistsException("Directory already exists.");
        }

        var parentDirectoryPath = PathUtil.removeResourceName(resourcePath);

        if (!storageService.objectExists(BucketName.USER_FILES, parentDirectoryPath)) {
            throw new ResourceNotFoundException("Parent directory does not exist.");
        }

        storageService.createDirectory(BucketName.USER_FILES, resourcePath);

        var createdPath = PathUtil.removeResourceName(path);
        var directoryName = PathUtil.extractResourceName(path);

        return new ResourceResponseDto(createdPath, directoryName, null, ResourceType.DIRECTORY);
    }
}
