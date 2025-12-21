package ru.redeyed.cloudstorage.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.common.util.RegexpUtil;
import ru.redeyed.cloudstorage.s3.BucketName;
import ru.redeyed.cloudstorage.s3.SimpleStorageService;
import ru.redeyed.cloudstorage.s3.StorageObjectInfo;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
public class ResourceService {

    private static final String SEARCH_PATTERN_FORMAT = "^.*%s.*$";

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

    public ResourceResponseDto moveResource(UUID userId, String fromPath, String toPath) {
        var fromResourcePath = ResourcePathUtil.createUserResourcePath(userId, fromPath);
        var toResourcePath = ResourcePathUtil.createUserResourcePath(userId, toPath);

        if (PathUtil.isDirectory(fromPath)) {
            return moveDirectory(fromResourcePath, toResourcePath);
        }

        return moveFile(fromResourcePath, toResourcePath);
    }

    private ResourceResponseDto moveDirectory(String fromPath, String toPath) {
        storageService.findDirectoryInfo(BucketName.USER_FILES, fromPath)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.DIRECTORY));

        if (storageService.directoryExists(BucketName.USER_FILES, toPath)) {
            throw new ResourceAlreadyExistsException(ResourceType.DIRECTORY);
        }

        storageService.moveDirectory(BucketName.USER_FILES, fromPath, toPath);

        var directoryPath = ResourcePathUtil.removeUserFolder(toPath);
        var directoryName = PathUtil.extractResourceName(toPath);

        return new ResourceResponseDto(directoryPath, directoryName, null, ResourceType.DIRECTORY);
    }

    private ResourceResponseDto moveFile(String fromPath, String toPath) {
        var fileInfo = storageService.findFileInfo(BucketName.USER_FILES, fromPath)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.FILE));

        if (storageService.fileExists(BucketName.USER_FILES, toPath)) {
            throw new ResourceAlreadyExistsException(ResourceType.FILE);
        }

        storageService.moveFile(BucketName.USER_FILES, fromPath, toPath);

        var filePath = ResourcePathUtil.removeUserFolder(toPath);
        var fileName = PathUtil.extractResourceName(toPath);

        return new ResourceResponseDto(filePath, fileName, fileInfo.size(), ResourceType.FILE);
    }

    public List<ResourceResponseDto> search(UUID userId, String query) {
        var path = ResourcePathUtil.createUserResourcePath(userId);

        query = RegexpUtil.escapeSpecialCharacters(query);

        var pattern = SEARCH_PATTERN_FORMAT.formatted(query);

        var foundObjectsInfo = storageService.search(BucketName.USER_FILES, path, pattern);

        return resourceMapper.toResourceResponseDtos(foundObjectsInfo);
    }
}
