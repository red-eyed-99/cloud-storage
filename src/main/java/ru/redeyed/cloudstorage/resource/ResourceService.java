package ru.redeyed.cloudstorage.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.s3.BucketName;
import ru.redeyed.cloudstorage.s3.SimpleStorageService;
import ru.redeyed.cloudstorage.s3.StorageObjectInfo;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private final SimpleStorageService storageService;

    private final ResourceMapper resourceMapper;

    public ResourceResponseDto getResource(UUID userId, String path) {
        var resourcePath = ResourcePathUtil.createUserResourcePath(userId, path);

        var storageObjectInfo = (StorageObjectInfo) null;

        if (PathUtil.isDirectory(path)) {
            storageObjectInfo = storageService.findDirectoryInfo(BucketName.USER_FILES, resourcePath)
                    .orElseThrow(() -> new ResourceNotFoundException(ResourceType.DIRECTORY));
        } else {
            storageObjectInfo = storageService.findFileInfo(BucketName.USER_FILES, resourcePath)
                    .orElseThrow(() -> new ResourceNotFoundException(ResourceType.FILE));
        }

        return resourceMapper.toResourceResponseDto(storageObjectInfo);
    }

    public void deleteResource(UUID userId, String path) {
        var resourcePath = ResourcePathUtil.createUserResourcePath(userId, path);

        if (PathUtil.isDirectory(path)) {
            storageService.removeDirectory(BucketName.USER_FILES, resourcePath);
        } else {
            storageService.removeFile(BucketName.USER_FILES, resourcePath);
        }
    }

    public List<ResourceResponseDto> getDirectoryContent(UUID userId, String path) {
        var directoryPath = ResourcePathUtil.createUserResourcePath(userId, path);

        if (!storageService.directoryExists(BucketName.USER_FILES, directoryPath)) {
            throw new ResourceNotFoundException(ResourceType.DIRECTORY);
        }

        var storageObjectsInfo = storageService.getDirectoryObjectsInfo(BucketName.USER_FILES, directoryPath, false);

        return resourceMapper.toResourceResponseDtos(storageObjectsInfo);
    }

    public ResourceResponseDto createDirectory(UUID userId, String path) {
        var directoryPath = ResourcePathUtil.createUserResourcePath(userId, path);

        if (storageService.directoryExists(BucketName.USER_FILES, directoryPath)) {
            throw new ResourceAlreadyExistsException(ResourceType.DIRECTORY);
        }

        var parentDirectoryPath = PathUtil.removeResourceName(directoryPath);

        if (!storageService.directoryExists(BucketName.USER_FILES, parentDirectoryPath)) {
            throw new ResourceNotFoundException("Parent directory does not exist.");
        }

        storageService.createDirectory(BucketName.USER_FILES, directoryPath);

        var createdPath = PathUtil.removeResourceName(path);
        var directoryName = PathUtil.extractResourceName(path);

        return new ResourceResponseDto(createdPath, directoryName, null, ResourceType.DIRECTORY);
    }

    public StreamingResponseBody downloadResource(UUID userId, String path) {
        var resourcePath = ResourcePathUtil.createUserResourcePath(userId, path);

        if (PathUtil.isDirectory(path)) {
            return downloadDirectory(resourcePath);
        }

        return downloadFile(resourcePath);
    }

    private StreamingResponseBody downloadDirectory(String path) {
        if (!storageService.directoryExists(BucketName.USER_FILES, path)) {
            throw new ResourceNotFoundException(ResourceType.DIRECTORY);
        }

        return outputStream -> {
            try (var zipOutputStream = new ZipOutputStream(outputStream)) {
                storageService.downloadDirectory(BucketName.USER_FILES, path, zipOutputStream);
            }
        };
    }

    private StreamingResponseBody downloadFile(String path) {
        if (!storageService.fileExists(BucketName.USER_FILES, path)) {
            throw new ResourceNotFoundException(ResourceType.FILE);
        }

        return outputStream -> {
            try (var inputStream = storageService.downloadFile(BucketName.USER_FILES, path)) {
                inputStream.transferTo(outputStream);
            }
        };
    }
}
