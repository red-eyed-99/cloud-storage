package ru.redeyed.cloudstorage.test.resource;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvFileSource;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
