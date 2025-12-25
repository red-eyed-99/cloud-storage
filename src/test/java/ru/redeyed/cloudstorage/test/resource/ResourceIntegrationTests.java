package ru.redeyed.cloudstorage.test.resource;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.resource.ResourcePathUtil;
import ru.redeyed.cloudstorage.resource.ResourceType;
import ru.redeyed.cloudstorage.s3.BucketName;
import ru.redeyed.cloudstorage.s3.SimpleStorageService;
import ru.redeyed.cloudstorage.test.auth.session.RedisSessionManager;
import ru.redeyed.cloudstorage.test.integration.BaseIntegrationTest;
import ru.redeyed.cloudstorage.test.resource.argumentsprovider.FileArgumentsProvider;
import ru.redeyed.cloudstorage.test.user.UserTestData;
import ru.redeyed.cloudstorage.util.ApiUtil;
import ru.redeyed.cloudstorage.util.MockFileCreator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Interaction with resources")
@RequiredArgsConstructor
public class ResourceIntegrationTests extends BaseIntegrationTest {

    private final MockMvc mockMvc;

    private final RedisSessionManager redisSessionManager;

    private final SimpleStorageService storageService;

    @AfterEach
    void removeCreatedObjects() {
        var userFilesPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID);
        storageService.removeDirectory(BucketName.USER_FILES, userFilesPath);
    }

    @Nested
    @DisplayName("Get information about resource")
    class GetResourceInfoTests {

        @ParameterizedTest(name = "{1}")
        @DisplayName("Get file info")
        @ArgumentsSource(FileArgumentsProvider.class)
        void shouldReturnFileInfo(MockMultipartFile file, String filePath) throws Exception {
            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            var rootPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID);
            var filePathWithoutName = PathUtil.removeResourceName(filePath);

            var expectedFilePath = filePathWithoutName.isEmpty()
                    ? PathUtil.PATH_DELIMITER
                    : filePathWithoutName;

            var expectedFileName = PathUtil.extractResourceName(filePath);

            storageService.uploadFiles(BucketName.USER_FILES, rootPath, List.of(file));

            mockMvc.perform(get(ApiUtil.RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_PATH_NAME, file.getOriginalFilename()))
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.path").value(expectedFilePath),
                            jsonPath("$.name").value(expectedFileName),
                            jsonPath("$.size").value(file.getSize()),
                            jsonPath("$.type").value(ResourceType.FILE.toString())
                    );
        }

        @ParameterizedTest
        @DisplayName("Get directory info")
        @MethodSource("ru.redeyed.cloudstorage.test.resource.ResourceIntegrationTests#getDirectoryPaths")
        void shouldReturnDirectoryInfo(String directoryPath) throws Exception {
            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            var path = ResourcePathUtil.createUserResourcePath(UserTestData.ID) + directoryPath;

            var directoryPathWithoutName = PathUtil.removeResourceName(directoryPath);

            var expectedDirectoryPath = directoryPathWithoutName.isEmpty()
                    ? PathUtil.PATH_DELIMITER
                    : directoryPathWithoutName;

            var expectedDirectoryName = PathUtil.extractResourceName(directoryPath);

            storageService.createDirectory(BucketName.USER_FILES, path);

            mockMvc.perform(get(ApiUtil.RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_PATH_NAME, directoryPath))
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.path").value(expectedDirectoryPath),
                            jsonPath("$.name").value(expectedDirectoryName),
                            jsonPath("$.size").doesNotExist(),
                            jsonPath("$.type").value(ResourceType.DIRECTORY.toString())
                    );
        }

        @ParameterizedTest
        @DisplayName("Resource doesn't exist")
        @ValueSource(strings = {"file.txt", "folder/"})
        void shouldReturnNotFound(String path) throws Exception {
            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_PATH_NAME, path))
                    .andExpect(status().isNotFound());
        }

        @ParameterizedTest
        @DisplayName("Invalid path parameter")
        @CsvFileSource(resources = "/data/invalid-resource-path-parameters.csv")
        void shouldReturnBadRequest(String path) throws Exception {
            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_PATH_NAME, path))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("User unauthorized")
        void shouldReturnUnauthorized() throws Exception {
            var guestSession = redisSessionManager.createGuestSession();
            var guestSessionInfo = redisSessionManager.getSessionInfo(guestSession);

            mockMvc.perform(get(ApiUtil.RESOURCE_URL)
                            .cookie(guestSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_PATH_NAME, "dummy"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Delete resource")
    class DeleteResourceTests {

        @ParameterizedTest
        @DisplayName("Delete file")
        @ArgumentsSource(FileArgumentsProvider.class)
        void shouldDeleteFile(MockMultipartFile file) throws Exception {
            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            var rootPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID);
            var filePath = file.getOriginalFilename();

            storageService.uploadFiles(BucketName.USER_FILES, rootPath, List.of(file));

            mockMvc.perform(delete(ApiUtil.RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_PATH_NAME, filePath))
                    .andExpect(status().isNoContent());

            assertFalse(storageService.fileExists(BucketName.USER_FILES, rootPath + filePath));
        }

        @ParameterizedTest
        @DisplayName("Delete directory")
        @MethodSource("ru.redeyed.cloudstorage.test.resource.ResourceIntegrationTests#getDirectoryPaths")
        void shouldDeleteDirectory(String directoryPath) throws Exception {
            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            var path = ResourcePathUtil.createUserResourcePath(UserTestData.ID) + directoryPath;

            storageService.createDirectory(BucketName.USER_FILES, path);

            mockMvc.perform(delete(ApiUtil.RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_PATH_NAME, directoryPath))
                    .andExpect(status().isNoContent());

            assertFalse(storageService.directoryExists(BucketName.USER_FILES, path));
        }

        @ParameterizedTest
        @DisplayName("Resource doesn't exist")
        @ValueSource(strings = {"file.txt", "folder/"})
        void shouldReturnNoContent(String path) throws Exception {
            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(delete(ApiUtil.RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_PATH_NAME, path))
                    .andExpect(status().isNoContent());
        }

        @ParameterizedTest
        @DisplayName("Invalid path parameter")
        @CsvFileSource(resources = "/data/invalid-resource-path-parameters.csv")
        void shouldReturnBadRequest(String path) throws Exception {
            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(delete(ApiUtil.RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_PATH_NAME, path))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("User unauthorized")
        void shouldReturnUnauthorized() throws Exception {
            var guestSession = redisSessionManager.createGuestSession();
            var guestSessionInfo = redisSessionManager.getSessionInfo(guestSession);

            mockMvc.perform(delete(ApiUtil.RESOURCE_URL)
                            .cookie(guestSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_PATH_NAME, "dummy"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Download resource")
    class DownloadResourceTests {

        @Test
        @DisplayName("Download file")
        void shouldDownloadFile() throws Exception {
            var filePath = "test.txt";
            var file = MockFileCreator.create(filePath, MockFileCreator.TEST_TEXT_CONTENT);

            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            var rootPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID);

            storageService.uploadFiles(BucketName.USER_FILES, rootPath, List.of(file));

            mockMvc.perform(get(ApiUtil.DOWNLOAD_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_PATH_NAME, filePath))
                    .andExpectAll(
                            status().isOk(),
                            content().contentType(MediaType.APPLICATION_OCTET_STREAM),
                            content().bytes(file.getBytes())
                    );
        }

        @Test
        @DisplayName("Download directory")
        void shouldDownloadZip() throws Exception {
            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            var resourcePaths = getDirectoryResourcePaths();

            var directoryName = PathUtil.extractRootParentDirectoryName(resourcePaths.getFirst());
            var directoryToDownload = directoryName + PathUtil.PATH_DELIMITER;

            var rootPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID);

            var files = new ArrayList<MultipartFile>();

            for (var path : resourcePaths) {
                if (PathUtil.isDirectory(path)) {
                    storageService.createDirectory(BucketName.USER_FILES, rootPath + path);
                    return;
                }

                var file = MockFileCreator.create(path, MockFileCreator.TEST_TEXT_CONTENT);

                files.add(file);
            }

            storageService.uploadFiles(BucketName.USER_FILES, rootPath, files);

            var mvcResult = mockMvc.perform(get(ApiUtil.DOWNLOAD_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_PATH_NAME, directoryToDownload))
                    .andExpectAll(
                            status().isOk(),
                            content().contentType(MediaType.APPLICATION_OCTET_STREAM)
                    )
                    .andReturn();

            mvcResult.getAsyncResult();

            var resultZip = mvcResult.getResponse().getContentAsByteArray();

            assertZipCorrect(resultZip, resourcePaths);
        }

        @ParameterizedTest
        @DisplayName("Resource doesn't exist")
        @ValueSource(strings = {"file.txt", "folder/"})
        void shouldReturnNotFound(String path) throws Exception {
            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.DOWNLOAD_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_PATH_NAME, path))
                    .andExpect(status().isNotFound());
        }

        @ParameterizedTest
        @DisplayName("Invalid path parameter")
        @CsvFileSource(resources = "/data/invalid-resource-path-parameters.csv")
        void shouldReturnBadRequest(String path) throws Exception {
            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.DOWNLOAD_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_PATH_NAME, path))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("User unauthorized")
        void shouldReturnUnauthorized() throws Exception {
            var guestSession = redisSessionManager.createGuestSession();
            var guestSessionInfo = redisSessionManager.getSessionInfo(guestSession);

            mockMvc.perform(get(ApiUtil.DOWNLOAD_RESOURCE_URL)
                            .cookie(guestSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_PATH_NAME, "dummy"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Move/rename resource")
    class MoveResourceTests {

        @Test
        @DisplayName("Move file")
        void shouldMoveFile() throws Exception {
            var filePath = "test.txt";
            var file = MockFileCreator.create(filePath);

            var rootPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID);
            var pathToMove = "folder1/test.txt";

            storageService.uploadFiles(BucketName.USER_FILES, rootPath, List.of(file));

            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.MOVE_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_FROM_PATH_NAME, filePath)
                            .queryParam(ApiUtil.REQUEST_PARAM_TO_PATH_NAME, pathToMove)
                    )
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.path").value(pathToMove),
                            jsonPath("$.name").value(PathUtil.extractResourceName(filePath)),
                            jsonPath("$.size").value(file.getSize()),
                            jsonPath("$.type").value(ResourceType.FILE.toString())
                    );

            assertAll(
                    () -> assertFalse(storageService.fileExists(BucketName.USER_FILES, rootPath + filePath)),
                    () -> assertTrue(storageService.fileExists(BucketName.USER_FILES, rootPath + pathToMove))
            );
        }

        @Test
        @DisplayName("Rename file")
        void shouldRenameFile() throws Exception {
            var filePath = "old-name.txt";
            var file = MockFileCreator.create(filePath);

            var rootPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID);
            var newFilePath = "new-name.txt";

            storageService.uploadFiles(BucketName.USER_FILES, rootPath, List.of(file));

            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.MOVE_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_FROM_PATH_NAME, filePath)
                            .queryParam(ApiUtil.REQUEST_PARAM_TO_PATH_NAME, newFilePath)
                    )
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.path").value(newFilePath),
                            jsonPath("$.name").value(PathUtil.extractResourceName(newFilePath)),
                            jsonPath("$.size").value(file.getSize()),
                            jsonPath("$.type").value(ResourceType.FILE.toString())
                    );

            assertAll(
                    () -> assertFalse(storageService.fileExists(BucketName.USER_FILES, rootPath + filePath)),
                    () -> assertTrue(storageService.fileExists(BucketName.USER_FILES, rootPath + newFilePath))
            );
        }

        @Test
        @DisplayName("Move empty directory")
        void shouldMoveDirectory() throws Exception {
            var oldDirectoryPath = "folder1/";
            var newDirectoryPath = "folder2/folder1/";

            var userOldDirectoryPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID, oldDirectoryPath);
            var userNewDirectoryPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID, newDirectoryPath);

            storageService.createDirectory(BucketName.USER_FILES, userOldDirectoryPath);

            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.MOVE_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_FROM_PATH_NAME, oldDirectoryPath)
                            .queryParam(ApiUtil.REQUEST_PARAM_TO_PATH_NAME, newDirectoryPath)
                    )
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.path").value(newDirectoryPath),
                            jsonPath("$.name").value(PathUtil.extractResourceName(oldDirectoryPath)),
                            jsonPath("$.size").doesNotExist(),
                            jsonPath("$.type").value(ResourceType.DIRECTORY.toString())
                    );

            assertAll(
                    () -> assertFalse(storageService.directoryExists(BucketName.USER_FILES, userOldDirectoryPath)),
                    () -> assertTrue(storageService.directoryExists(BucketName.USER_FILES, userNewDirectoryPath))
            );
        }

        @Test
        @DisplayName("Rename empty directory")
        void shouldRenameDirectory() throws Exception {
            var oldDirectoryPath = "folder1/";
            var newDirectoryPath = "folder2/";

            var userOldDirectoryPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID, oldDirectoryPath);
            var userNewDirectoryPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID, newDirectoryPath);

            storageService.createDirectory(BucketName.USER_FILES, userOldDirectoryPath);

            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.MOVE_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_FROM_PATH_NAME, oldDirectoryPath)
                            .queryParam(ApiUtil.REQUEST_PARAM_TO_PATH_NAME, newDirectoryPath)
                    )
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.path").value(newDirectoryPath),
                            jsonPath("$.name").value(PathUtil.extractResourceName(newDirectoryPath)),
                            jsonPath("$.size").doesNotExist(),
                            jsonPath("$.type").value(ResourceType.DIRECTORY.toString())
                    );

            assertAll(
                    () -> assertFalse(storageService.directoryExists(BucketName.USER_FILES, userOldDirectoryPath)),
                    () -> assertTrue(storageService.directoryExists(BucketName.USER_FILES, userNewDirectoryPath))
            );
        }

        @Test
        @DisplayName("Move directory with resources")
        void shouldMoveDirectoryWithResources() throws Exception {
            var userFilesPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID);

            var oldDirectoryPath = "folder1/";
            var oldSubdirectoryPath = oldDirectoryPath + "sub-folder/";

            var newDirectoryPath = "folder2/folder1/";
            var newSubdirectoryPath = newDirectoryPath + "sub-folder/";

            var userOldDirectoryPath = userFilesPath + oldDirectoryPath;
            var userNewDirectoryPath = userFilesPath + newDirectoryPath;

            var firstFileName = "file1.txt";
            var secondFileName = "file2.txt";

            var firstFile = MockFileCreator.create(oldDirectoryPath + firstFileName);
            var secondFile = MockFileCreator.create(oldSubdirectoryPath + secondFileName);

            storageService.uploadFiles(BucketName.USER_FILES, userFilesPath, List.of(firstFile, secondFile));

            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.MOVE_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_FROM_PATH_NAME, oldDirectoryPath)
                            .queryParam(ApiUtil.REQUEST_PARAM_TO_PATH_NAME, newDirectoryPath)
                    )
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.path").value(newDirectoryPath),
                            jsonPath("$.name").value(PathUtil.extractResourceName(oldDirectoryPath)),
                            jsonPath("$.size").doesNotExist(),
                            jsonPath("$.type").value(ResourceType.DIRECTORY.toString())
                    );

            var subdirectoryExpectedPath = userFilesPath + newSubdirectoryPath;
            var firstFileExpectedPath = userNewDirectoryPath + firstFileName;
            var secondFileExpectedPath = userFilesPath + newSubdirectoryPath + secondFileName;

            assertAll(
                    () -> assertFalse(storageService.directoryExists(BucketName.USER_FILES, userOldDirectoryPath)),
                    () -> assertTrue(storageService.directoryExists(BucketName.USER_FILES, userNewDirectoryPath)),
                    () -> assertTrue(storageService.directoryExists(BucketName.USER_FILES, subdirectoryExpectedPath)),
                    () -> assertTrue(storageService.fileExists(BucketName.USER_FILES, firstFileExpectedPath)),
                    () -> assertTrue(storageService.fileExists(BucketName.USER_FILES, secondFileExpectedPath))
            );
        }

        @Test
        @DisplayName("Rename directory with resources")
        void shouldRenameDirectoryWithResources() throws Exception {
            var userFilesPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID);

            var oldDirectoryPath = "folder1/";
            var oldSubdirectoryPath = oldDirectoryPath + "sub-folder/";

            var newDirectoryPath = "folder2/";
            var newSubdirectoryPath = newDirectoryPath + "sub-folder/";

            var userOldDirectoryPath = userFilesPath + oldDirectoryPath;
            var userNewDirectoryPath = userFilesPath + newDirectoryPath;

            var firstFileName = "file1.txt";
            var secondFileName = "file2.txt";

            var firstFile = MockFileCreator.create(oldDirectoryPath + firstFileName);
            var secondFile = MockFileCreator.create(oldSubdirectoryPath + secondFileName);

            storageService.uploadFiles(BucketName.USER_FILES, userFilesPath, List.of(firstFile, secondFile));

            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.MOVE_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_FROM_PATH_NAME, oldDirectoryPath)
                            .queryParam(ApiUtil.REQUEST_PARAM_TO_PATH_NAME, newDirectoryPath)
                    )
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.path").value(newDirectoryPath),
                            jsonPath("$.name").value(PathUtil.extractResourceName(newDirectoryPath)),
                            jsonPath("$.size").doesNotExist(),
                            jsonPath("$.type").value(ResourceType.DIRECTORY.toString())
                    );

            var subdirectoryExpectedPath = userFilesPath + newSubdirectoryPath;
            var firstFileExpectedPath = userNewDirectoryPath + firstFileName;
            var secondFileExpectedPath = userFilesPath + newSubdirectoryPath + secondFileName;

            assertAll(
                    () -> assertFalse(storageService.directoryExists(BucketName.USER_FILES, userOldDirectoryPath)),
                    () -> assertTrue(storageService.directoryExists(BucketName.USER_FILES, userNewDirectoryPath)),
                    () -> assertTrue(storageService.directoryExists(BucketName.USER_FILES, subdirectoryExpectedPath)),
                    () -> assertTrue(storageService.fileExists(BucketName.USER_FILES, firstFileExpectedPath)),
                    () -> assertTrue(storageService.fileExists(BucketName.USER_FILES, secondFileExpectedPath))
            );
        }

        @Test
        @DisplayName("File already exists")
        void shouldReturnConflictWhenFileExists() throws Exception {
            var filePath = "file.txt";
            var file = MockFileCreator.create(filePath);

            var userFilesPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID);

            storageService.uploadFiles(BucketName.USER_FILES, userFilesPath, List.of(file));

            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.MOVE_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_FROM_PATH_NAME, filePath)
                            .queryParam(ApiUtil.REQUEST_PARAM_TO_PATH_NAME, filePath))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Directory already exists")
        void shouldReturnConflictWhenDirectoryExists() throws Exception {
            var directoryPath = "folder/";
            var userFilesPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID);
            var path = userFilesPath + directoryPath;

            storageService.createDirectory(BucketName.USER_FILES, path);

            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.MOVE_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_FROM_PATH_NAME, directoryPath)
                            .queryParam(ApiUtil.REQUEST_PARAM_TO_PATH_NAME, directoryPath))
                    .andExpect(status().isConflict());
        }

        @ParameterizedTest
        @DisplayName("Resource doesn't exist")
        @CsvSource({"file.txt,file1.txt", "folder1/,folder2/"})
        void shouldReturnNotFound(String fromPath, String toPath) throws Exception {
            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.MOVE_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_FROM_PATH_NAME, fromPath)
                            .queryParam(ApiUtil.REQUEST_PARAM_TO_PATH_NAME, toPath))
                    .andExpect(status().isNotFound());
        }

        @ParameterizedTest
        @DisplayName("Invalid 'from' path parameter")
        @CsvFileSource(resources = "/data/invalid-resource-path-parameters.csv")
        void shouldReturnBadRequestWhenFromPathIsInvalid(String path) throws Exception {
            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.MOVE_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_FROM_PATH_NAME, path)
                            .queryParam(ApiUtil.REQUEST_PARAM_TO_PATH_NAME, "correct"))
                    .andExpect(status().isBadRequest());
        }

        @ParameterizedTest
        @DisplayName("Invalid 'to' path parameter")
        @CsvFileSource(resources = "/data/invalid-resource-path-parameters.csv")
        void shouldReturnBadRequestWhenToPathIsInvalid(String path) throws Exception {
            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.MOVE_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_FROM_PATH_NAME, "correct")
                            .queryParam(ApiUtil.REQUEST_PARAM_TO_PATH_NAME, path))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("User unauthorized")
        void shouldReturnUnauthorized() throws Exception {
            var guestSession = redisSessionManager.createGuestSession();
            var guestSessionInfo = redisSessionManager.getSessionInfo(guestSession);

            mockMvc.perform(get(ApiUtil.MOVE_RESOURCE_URL)
                            .cookie(guestSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_FROM_PATH_NAME, "dummy")
                            .queryParam(ApiUtil.REQUEST_PARAM_TO_PATH_NAME, "dummy"))
                    .andExpect(status().isUnauthorized());
        }
    }

    private static Stream<Arguments> getDirectoryPaths() {
        return Stream.of(
                Arguments.of("test-directory/"),
                Arguments.of("test-directory-1/test-directory-2/"),
                Arguments.of("test-directory-1/test-directory-2/test-directory-3/")
        );
    }

    private static List<String> getDirectoryResourcePaths() {
        return List.of(
                "folder1/file.txt",
                "folder1/folder2-2/",
                "folder1/folder2-1/file",
                "folder1/folder2/file.txt",
                "folder1/folder2/folder3/file.txt"
        );
    }

    private static void assertZipCorrect(byte[] zip, List<String> resourcePaths) throws IOException {
        try (var zipInputStream = new ZipInputStream(new ByteArrayInputStream(zip))) {
            var currentResourceNumber = 0;

            while (currentResourceNumber < resourcePaths.size()) {
                var zipEntry = Objects.requireNonNull(zipInputStream.getNextEntry());
                var resourcePath = resourcePaths.get(currentResourceNumber++);
                assertEquals(resourcePath, zipEntry.getName());
            }
        }
    }

}
