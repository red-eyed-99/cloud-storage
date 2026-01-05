package ru.redeyed.cloudstorage.resource;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.common.util.RegexpUtil;
import ru.redeyed.cloudstorage.resource.dto.ResourceResponseDto;
import ru.redeyed.cloudstorage.resource.exception.FileExtensionChangedException;
import ru.redeyed.cloudstorage.resource.exception.ResourceAlreadyExistsException;
import ru.redeyed.cloudstorage.resource.exception.ResourceNotFoundException;
import ru.redeyed.cloudstorage.s3.BucketName;
import ru.redeyed.cloudstorage.s3.SimpleStorageService;
import ru.redeyed.cloudstorage.s3.StorageObjectInfo;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
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
            if (!ResourcePathUtil.isUserFolder(directoryPath)) {
                throw new ResourceNotFoundException(ResourceType.DIRECTORY);
            }
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
            if (!ResourcePathUtil.isUserFolder(parentDirectoryPath)) {
                throw new ResourceNotFoundException("Parent directory does not exist.");
            }
        }

        validateObjectsConflict(directoryPath);

        storageService.createDirectory(BucketName.USER_FILES, directoryPath);

        var createdPath = StringUtils.defaultIfEmpty(
                PathUtil.removeResourceName(path),
                PathUtil.PATH_DELIMITER
        );

        var directoryName = PathUtil.extractResourceName(path);

        return new ResourceResponseDto(createdPath, directoryName, null, ResourceType.DIRECTORY);
    }

    public List<ResourceResponseDto> uploadFiles(UUID userId, String path, List<MultipartFile> files) {
        var uploadedResourcesInfo = new ArrayList<StorageObjectInfo>();

        var userFilesPath = getUserFilesPath(userId, path);

        var checkedDirectoriesPaths = new HashSet<String>();

        for (var file : files) {
            var filePath = Objects.requireNonNull(file.getOriginalFilename());

            if (PathUtil.isFileName(filePath)) {
                var fullFilePath = userFilesPath + filePath;
                checkFileNotExists(fullFilePath);
                validateObjectsConflict(fullFilePath);
                continue;
            }

            var rootDirectoryName = PathUtil.extractRootParentDirectoryName(filePath);
            var rootDirectoryPath = userFilesPath + rootDirectoryName + PathUtil.PATH_DELIMITER;

            if (!checkedDirectoriesPaths.contains(rootDirectoryPath)) {
                var rootDirectoryInfo = createRootDirectory(rootDirectoryPath);
                checkedDirectoriesPaths.add(rootDirectoryPath);
                uploadedResourcesInfo.add(rootDirectoryInfo);
            }

            var nestedDirectoriesInfo = createNestedDirectories(userFilesPath, filePath, checkedDirectoriesPaths);

            uploadedResourcesInfo.addAll(nestedDirectoriesInfo);
        }

        var uploadedFilesInfo = storageService.uploadFiles(BucketName.USER_FILES, userFilesPath, files);

        uploadedResourcesInfo.addAll(uploadedFilesInfo);

        return resourceMapper.toResourceResponseDtos(uploadedResourcesInfo);
    }

    private String getUserFilesPath(UUID userId, String path) {
        return PathUtil.isRootDirectory(path)
                ? ResourcePathUtil.createUserResourcePath(userId)
                : ResourcePathUtil.createUserResourcePath(userId, path);
    }

    private void checkFileNotExists(String fullFilePath) {
        var fileExists = storageService.fileExists(BucketName.USER_FILES, fullFilePath);

        if (fileExists) {
            throw new ResourceAlreadyExistsException(ResourceType.FILE);
        }
    }

    private StorageObjectInfo createRootDirectory(String rootDirectoryPath) {
        if (storageService.directoryExists(BucketName.USER_FILES, rootDirectoryPath)) {
            throw new ResourceAlreadyExistsException(ResourceType.DIRECTORY);
        }

        validateObjectsConflict(rootDirectoryPath);

        return storageService.createDirectory(BucketName.USER_FILES, rootDirectoryPath);
    }

    private List<StorageObjectInfo> createNestedDirectories(String userFilesPath, String filePath,
                                                            Set<String> checkedDirectoriesPaths) {

        var createdDirectoriesInfo = new ArrayList<StorageObjectInfo>();

        var parentDirectoryPath = userFilesPath + PathUtil.removeResourceName(filePath);

        while (!parentDirectoryPath.equals(userFilesPath)) {

            if (!checkedDirectoriesPaths.contains(parentDirectoryPath)) {
                var directoryExists = storageService.directoryExists(BucketName.USER_FILES, parentDirectoryPath);

                if (!directoryExists) {
                    var directoryInfo = storageService.createDirectory(BucketName.USER_FILES, parentDirectoryPath);
                    createdDirectoriesInfo.add(directoryInfo);
                }
            }

            checkedDirectoriesPaths.add(parentDirectoryPath);

            parentDirectoryPath = PathUtil.removeResourceName(parentDirectoryPath);
        }

        return createdDirectoriesInfo;
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

        validateObjectsConflict(toResourcePath);

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

        directoryPath = PathUtil.removeResourceName(directoryPath);
        directoryPath = directoryPath.isEmpty() ? PathUtil.PATH_DELIMITER : directoryPath;

        var directoryName = PathUtil.extractResourceName(toPath);

        return new ResourceResponseDto(directoryPath, directoryName, null, ResourceType.DIRECTORY);
    }

    private ResourceResponseDto moveFile(String fromPath, String toPath) {
        var fileInfo = storageService.findFileInfo(BucketName.USER_FILES, fromPath)
                .orElseThrow(() -> new ResourceNotFoundException(ResourceType.FILE));

        if (fileExtensionChanged(fromPath, toPath)) {
            throw new FileExtensionChangedException();
        }

        if (storageService.fileExists(BucketName.USER_FILES, toPath)) {
            throw new ResourceAlreadyExistsException(ResourceType.FILE);
        }

        storageService.moveFile(BucketName.USER_FILES, fromPath, toPath);

        var filePath = ResourcePathUtil.removeUserFolder(toPath);

        filePath = PathUtil.removeResourceName(filePath);
        filePath = filePath.isEmpty() ? PathUtil.PATH_DELIMITER : filePath;

        var fileName = PathUtil.extractResourceName(toPath);

        return new ResourceResponseDto(filePath, fileName, fileInfo.size(), ResourceType.FILE);
    }

    private boolean fileExtensionChanged(String fromPath, String toPath) {
        var fromFileExtension = PathUtil.extractFileExtension(fromPath);
        var toFileExtension = PathUtil.extractFileExtension(toPath);
        return !fromFileExtension.equals(toFileExtension);
    }

    public List<ResourceResponseDto> search(UUID userId, String query) {
        var path = ResourcePathUtil.createUserResourcePath(userId);

        query = RegexpUtil.escapeSpecialCharacters(query);

        var pattern = SEARCH_PATTERN_FORMAT.formatted(query);

        var foundObjectsInfo = storageService.search(BucketName.USER_FILES, path, pattern);

        foundObjectsInfo.removeIf(objectInfo -> ResourcePathUtil.isUserFolder(objectInfo.path()));

        return resourceMapper.toResourceResponseDtos(foundObjectsInfo);
    }

    private void validateObjectsConflict(String path) {
        var message = "It is not allowed to create a file and a folder with the same name in the same directory";

        if (PathUtil.isDirectory(path)) {
            var filePath = PathUtil.trimLastSlash(path);

            if (storageService.fileExists(BucketName.USER_FILES, filePath)) {
                throw new ResourceAlreadyExistsException(message);
            }
        }

        var directoryPath = path + PathUtil.PATH_DELIMITER;

        if (storageService.directoryExists(BucketName.USER_FILES, directoryPath)) {
            throw new ResourceAlreadyExistsException(message);
        }
    }
}
