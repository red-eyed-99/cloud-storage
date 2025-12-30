package ru.redeyed.cloudstorage.test.resource.argumentsprovider;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.springframework.mock.web.MockMultipartFile;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.resource.ResourceType;
import ru.redeyed.cloudstorage.resource.dto.ResourceResponseDto;
import ru.redeyed.cloudstorage.test.resource.ResourceManager;
import ru.redeyed.cloudstorage.test.resource.ResourcePaths;
import ru.redeyed.cloudstorage.util.JsonUtil;
import ru.redeyed.cloudstorage.util.MockFileCreator;
import tools.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class UploadResourcesArgumentsProvider implements ArgumentsProvider {

    @Override
    public @NotNull Stream<? extends Arguments> provideArguments(@NotNull ParameterDeclarations parameters,
                                                                 @NotNull ExtensionContext context) {
        return Stream.of(
                getUploadFilesInRootArguments(),
                getUploadFilesInFolderArguments(),
                getUploadDirectoryWithFilesInRootArguments(),
                getUploadDirectoryWithFilesInFolderArguments()
        );
    }

    private Arguments getUploadFilesInRootArguments() {
        var path = PathUtil.PATH_DELIMITER;

        var expectedResponseJson = JsonUtil.getJsonFrom("data/responses/upload/upload-files-in-root.json");

        var resourceResponseDtos = getResourceResponseDtos(expectedResponseJson);

        var files = new ArrayList<MockMultipartFile>();

        var expectedFileExistsPaths = new ArrayList<String>();

        for (var resourceResponseDto : resourceResponseDtos) {
            var file = MockFileCreator.createDefault(resourceResponseDto.name());

            files.add(file);

            var expectedFilePath = getExpectedFilePath(resourceResponseDto);

            expectedFileExistsPaths.add(expectedFilePath);
        }

        return Arguments.of(path, files, expectedFileExistsPaths, expectedResponseJson);
    }

    private Arguments getUploadFilesInFolderArguments() {
        var path = ResourcePaths.FOLDER_1;

        var expectedResponseJson = JsonUtil.getJsonFrom("data/responses/upload/upload-files-in-folder.json");

        var resourceResponseDtos = getResourceResponseDtos(expectedResponseJson);

        var files = new ArrayList<MockMultipartFile>();

        var expectedFileExistsPaths = new ArrayList<String>();

        for (var resourceResponseDto : resourceResponseDtos) {
            var file = MockFileCreator.createDefault(resourceResponseDto.name());

            files.add(file);

            var expectedFilePath = getExpectedFilePath(resourceResponseDto);

            expectedFileExistsPaths.add(expectedFilePath);
        }

        return Arguments.of(path, files, expectedFileExistsPaths, expectedResponseJson);
    }

    private Arguments getUploadDirectoryWithFilesInRootArguments() {
        var path = PathUtil.PATH_DELIMITER;

        var expectedResponseJson = JsonUtil.getJsonFrom("data/responses/upload/upload-directory-with-files-in-root.json");

        var resourceResponseDtos = getResourceResponseDtos(expectedResponseJson);

        var files = new ArrayList<MockMultipartFile>();

        var expectedResourceExistsPaths = new ArrayList<String>();

        for (var resourceResponseDto : resourceResponseDtos) {

            if (resourceResponseDto.type() == ResourceType.DIRECTORY) {
                var expectedDirectoryPath = getExpectedDirectoryPath(resourceResponseDto);
                expectedResourceExistsPaths.add(expectedDirectoryPath);
                continue;
            }

            var file = createFileInFolder(resourceResponseDto, false);

            files.add(file);

            var expectedFilePath = getExpectedFilePath(resourceResponseDto);

            expectedResourceExistsPaths.add(expectedFilePath);
        }

        return Arguments.of(path, files, expectedResourceExistsPaths, expectedResponseJson);
    }

    private Arguments getUploadDirectoryWithFilesInFolderArguments() {
        var path = ResourcePaths.FOLDER_1;

        var expectedResponseJson = JsonUtil.getJsonFrom("data/responses/upload/upload-directory-with-files-in-folder.json");

        var resourceResponseDtos = getResourceResponseDtos(expectedResponseJson);

        var files = new ArrayList<MockMultipartFile>();

        var expectedResourceExistsPaths = new ArrayList<String>();

        for (var resourceResponseDto : resourceResponseDtos) {

            if (resourceResponseDto.type() == ResourceType.DIRECTORY) {
                var expectedDirectoryPath = getExpectedDirectoryPath(resourceResponseDto);
                expectedResourceExistsPaths.add(expectedDirectoryPath);
                continue;
            }

            var file = createFileInFolder(resourceResponseDto, true);

            files.add(file);

            var expectedFilePath = getExpectedFilePath(resourceResponseDto);

            expectedResourceExistsPaths.add(expectedFilePath);
        }

        return Arguments.of(path, files, expectedResourceExistsPaths, expectedResponseJson);
    }

    private List<ResourceResponseDto> getResourceResponseDtos(String responseJson) {
        return JsonUtil.fromJson(responseJson, new TypeReference<>() {
        });
    }

    private String getExpectedDirectoryPath(ResourceResponseDto resourceResponseDto) {
        var stringBuilder = new StringBuilder(ResourceManager.USER_FILES_DIR_PATH);

        var path = resourceResponseDto.path();

        if (!PathUtil.isRootDirectory(path)) {
            stringBuilder.append(path);
        }

        stringBuilder.append(resourceResponseDto.name());
        stringBuilder.append(PathUtil.PATH_DELIMITER);

        return stringBuilder.toString();
    }

    private String getExpectedFilePath(ResourceResponseDto resourceResponseDto) {
        var stringBuilder = new StringBuilder(ResourceManager.USER_FILES_DIR_PATH);

        var path = resourceResponseDto.path();

        if (!PathUtil.isRootDirectory(path)) {
            stringBuilder.append(path);
        }

        stringBuilder.append(resourceResponseDto.name());

        return stringBuilder.toString();
    }

    private MockMultipartFile createFileInFolder(ResourceResponseDto resourceResponseDto, boolean trimRootDirectory) {
        var path = resourceResponseDto.path() + resourceResponseDto.name();

        if (trimRootDirectory) {
            path = PathUtil.removeRootParentDirectory(path);
        }

        return MockFileCreator.createDefault(path);
    }
}
