package ru.redeyed.cloudstorage.test.resource;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.redeyed.cloudstorage.resource.ResourcePathUtil;
import ru.redeyed.cloudstorage.resource.ResourceType;
import ru.redeyed.cloudstorage.s3.BucketName;
import ru.redeyed.cloudstorage.s3.SimpleStorageService;
import ru.redeyed.cloudstorage.test.auth.session.RedisSessionManager;
import ru.redeyed.cloudstorage.test.integration.BaseIntegrationTest;
import ru.redeyed.cloudstorage.test.resource.argumentsprovider.ResourceAlreadyExistsArgumentsProvider;
import ru.redeyed.cloudstorage.test.resource.argumentsprovider.SearchResourcesByQueryArgumentsProvider;
import ru.redeyed.cloudstorage.test.resource.argumentsprovider.directory.DownloadDirectoryArgumentsProvider;
import ru.redeyed.cloudstorage.test.resource.argumentsprovider.directory.GetDirectoryInfoArgumentsProvider;
import ru.redeyed.cloudstorage.test.resource.argumentsprovider.directory.MoveDirectoryArgumentsProvider;
import ru.redeyed.cloudstorage.test.resource.argumentsprovider.directory.RenameDirectoryArgumentsProvider;
import ru.redeyed.cloudstorage.test.resource.argumentsprovider.file.DownloadFileArgumentsProvider;
import ru.redeyed.cloudstorage.test.resource.argumentsprovider.file.GetFileInfoArgumentsProvider;
import ru.redeyed.cloudstorage.test.resource.argumentsprovider.file.MoveFileArgumentsProvider;
import ru.redeyed.cloudstorage.test.resource.argumentsprovider.file.RenameFileArgumentsProvider;
import ru.redeyed.cloudstorage.test.user.UserTestData;
import ru.redeyed.cloudstorage.util.ApiUtil;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
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

    private final ResourceManager resourceManager;

    private final SimpleStorageService storageService;

    @AfterEach
    void clearResources() {
        resourceManager.clearResources();
    }

    @Nested
    @DisplayName("Get information about resource")
    class GetResourceInfoTests {

        @ParameterizedTest
        @DisplayName("Get file info")
        @ArgumentsSource(GetFileInfoArgumentsProvider.class)
        @SneakyThrows
        void shouldReturnFileInfo(String path, String expectedPath, String expectedName, long expectedSize) {
            resourceManager.createDefaultResources();

            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_PATH_NAME, path))
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.path").value(expectedPath),
                            jsonPath("$.name").value(expectedName),
                            jsonPath("$.size").value(expectedSize),
                            jsonPath("$.type").value(ResourceType.FILE.toString())
                    );
        }

        @ParameterizedTest
        @DisplayName("Get directory info")
        @ArgumentsSource(GetDirectoryInfoArgumentsProvider.class)
        void shouldReturnDirectoryInfo(String path, String expectedPath, String expectedName) throws Exception {
            resourceManager.createDefaultResources();

            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_PATH_NAME, path))
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.path").value(expectedPath),
                            jsonPath("$.name").value(expectedName),
                            jsonPath("$.size").doesNotExist(),
                            jsonPath("$.type").value(ResourceType.DIRECTORY.toString())
                    );
        }

        @ParameterizedTest
        @DisplayName("Resource doesn't exist")
        @ValueSource(strings = {"non-existent-file.txt", "non-existent-folder/"})
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
        @ValueSource(strings = {
                ResourcePaths.UNDEFINED_FILE_1, ResourcePaths.FILE_1_TXT, ResourcePaths.FOLDER_1_FILE_2_TXT,
                ResourcePaths.FOLDER_1_FOLDER_2_FILE_3_TXT
        })
        void shouldDeleteFile(String path) throws Exception {
            var userFilePath = ResourcePathUtil.createUserResourcePath(UserTestData.ID, path);

            resourceManager.createDefaultResources();

            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(delete(ApiUtil.RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_PATH_NAME, path))
                    .andExpect(status().isNoContent());

            assertFalse(storageService.objectExists(BucketName.USER_FILES, userFilePath));
        }

        @ParameterizedTest
        @DisplayName("Delete directory")
        @ValueSource(strings = {
                ResourcePaths.FOLDER_1, ResourcePaths.FOLDER_1_FOLDER_2, ResourcePaths.FOLDER_1_FOLDER_2_FOLDER_3,
                ResourcePaths.FOLDER_4
        })
        void shouldDeleteDirectory(String path) throws Exception {
            var userDirectoryPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID, path);

            resourceManager.createDefaultResources();

            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(delete(ApiUtil.RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_PATH_NAME, path))
                    .andExpect(status().isNoContent());

            assertFalse(storageService.objectExists(BucketName.USER_FILES, userDirectoryPath));
        }

        @ParameterizedTest
        @DisplayName("Resource doesn't exist")
        @ValueSource(strings = {"non-existent-file.txt", "non-existent-folder/"})
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

        @ParameterizedTest
        @DisplayName("Download file")
        @ArgumentsSource(DownloadFileArgumentsProvider.class)
        void shouldDownloadFile(String path, byte[] expectedContent) throws Exception {
            resourceManager.createDefaultResources();

            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.DOWNLOAD_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_PATH_NAME, path))
                    .andExpectAll(
                            status().isOk(),
                            content().contentType(MediaType.APPLICATION_OCTET_STREAM),
                            content().bytes(expectedContent)
                    );
        }

        @ParameterizedTest
        @DisplayName("Download directory")
        @ArgumentsSource(DownloadDirectoryArgumentsProvider.class)
        void shouldDownloadZip(String path, List<String> resourcePaths) throws Exception {
            resourceManager.createDefaultResources();

            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            var mvcResult = mockMvc.perform(get(ApiUtil.DOWNLOAD_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_PATH_NAME, path))
                    .andExpectAll(
                            status().isOk(),
                            content().contentType(MediaType.APPLICATION_OCTET_STREAM)
                    )
                    .andReturn();

            mvcResult.getAsyncResult();

            var resultZip = mvcResult.getResponse().getContentAsByteArray();

            assertZipCorrect(resultZip, resourcePaths);
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

        @ParameterizedTest
        @DisplayName("Resource doesn't exist")
        @ValueSource(strings = {"non-existent-file.txt", "non-existent-folder/"})
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

        @ParameterizedTest
        @DisplayName("Move file")
        @ArgumentsSource(MoveFileArgumentsProvider.class)
        @SneakyThrows
        void shouldMoveFile(
                String from, String to,
                String expectedFilePath, String expectedFileName, long expectedFileSize
        ) {
            var userFileFromPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID, from);
            var userFileToPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID, to);

            resourceManager.createDefaultResources();

            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.MOVE_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_FROM_PATH_NAME, from)
                            .queryParam(ApiUtil.REQUEST_PARAM_TO_PATH_NAME, to))
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.path").value(expectedFilePath),
                            jsonPath("$.name").value(expectedFileName),
                            jsonPath("$.size").value(expectedFileSize),
                            jsonPath("$.type").value(ResourceType.FILE.toString())
                    );

            assertAll(
                    () -> assertFalse(storageService.objectExists(BucketName.USER_FILES, userFileFromPath)),
                    () -> assertTrue(storageService.objectExists(BucketName.USER_FILES, userFileToPath))
            );
        }

        @ParameterizedTest
        @DisplayName("Rename file")
        @ArgumentsSource(RenameFileArgumentsProvider.class)
        @SneakyThrows
        void shouldRenameFile(
                String from, String to,
                String expectedFilePath, String expectedFileName, long expectedFileSize
        ) {
            var userFileFromPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID, from);
            var userFileToPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID, to);

            resourceManager.createDefaultResources();

            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.MOVE_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_FROM_PATH_NAME, from)
                            .queryParam(ApiUtil.REQUEST_PARAM_TO_PATH_NAME, to))
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.path").value(expectedFilePath),
                            jsonPath("$.name").value(expectedFileName),
                            jsonPath("$.size").value(expectedFileSize),
                            jsonPath("$.type").value(ResourceType.FILE.toString())
                    );

            assertAll(
                    () -> assertFalse(storageService.objectExists(BucketName.USER_FILES, userFileFromPath)),
                    () -> assertTrue(storageService.objectExists(BucketName.USER_FILES, userFileToPath))
            );
        }

        @ParameterizedTest
        @DisplayName("Move directory")
        @ArgumentsSource(MoveDirectoryArgumentsProvider.class)
        @SneakyThrows
        void shouldMoveDirectory(
                String from, String to,
                String expectedDirectoryPath, String expectedDirectoryName, List<String> expectedExistResourcePaths
        ) {
            var userOldDirectoryPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID, from);

            resourceManager.createDefaultResources();

            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.MOVE_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_FROM_PATH_NAME, from)
                            .queryParam(ApiUtil.REQUEST_PARAM_TO_PATH_NAME, to))
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.path").value(expectedDirectoryPath),
                            jsonPath("$.name").value(expectedDirectoryName),
                            jsonPath("$.size").doesNotExist(),
                            jsonPath("$.type").value(ResourceType.DIRECTORY.toString())
                    );

            assertFalse(storageService.objectExists(BucketName.USER_FILES, userOldDirectoryPath));

            for (var resourcePath : expectedExistResourcePaths) {
                assertTrue(storageService.objectExists(BucketName.USER_FILES, resourcePath));
            }
        }

        @ParameterizedTest
        @DisplayName("Rename directory")
        @ArgumentsSource(RenameDirectoryArgumentsProvider.class)
        @SneakyThrows
        void shouldRenameDirectory(
                String from, String to,
                String expectedDirectoryPath, String expectedDirectoryName, List<String> expectedExistResourcePaths
        ) {
            var userOldDirectoryPath = ResourcePathUtil.createUserResourcePath(UserTestData.ID, from);

            resourceManager.createDefaultResources();

            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.MOVE_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_FROM_PATH_NAME, from)
                            .queryParam(ApiUtil.REQUEST_PARAM_TO_PATH_NAME, to))
                    .andExpectAll(
                            status().isOk(),
                            jsonPath("$.path").value(expectedDirectoryPath),
                            jsonPath("$.name").value(expectedDirectoryName),
                            jsonPath("$.size").doesNotExist(),
                            jsonPath("$.type").value(ResourceType.DIRECTORY.toString())
                    );

            assertFalse(storageService.objectExists(BucketName.USER_FILES, userOldDirectoryPath));

            for (var resourcePath : expectedExistResourcePaths) {
                assertTrue(storageService.objectExists(BucketName.USER_FILES, resourcePath));
            }
        }

        @ParameterizedTest
        @DisplayName("Resource already exists")
        @ArgumentsSource(ResourceAlreadyExistsArgumentsProvider.class)
        void shouldReturnConflict(String from, String to) throws Exception {
            resourceManager.createDefaultResources();

            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.MOVE_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_FROM_PATH_NAME, from)
                            .queryParam(ApiUtil.REQUEST_PARAM_TO_PATH_NAME, to))
                    .andExpect(status().isConflict());
        }

        @ParameterizedTest
        @DisplayName("Resource doesn't exist")
        @CsvSource({
                "non-existent-file.txt,file.txt",
                "non-existent-folder/,folder/",
                "non-existent-file.txt,folder/non-existent-file.txt",
                "non-existent-folder/,folder/non-existent-folder/,"
        })
        void shouldReturnNotFound(String from, String to) throws Exception {
            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.MOVE_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_FROM_PATH_NAME, from)
                            .queryParam(ApiUtil.REQUEST_PARAM_TO_PATH_NAME, to))
                    .andExpect(status().isNotFound());
        }

        @ParameterizedTest
        @DisplayName("Invalid 'from' or 'to' path parameters")
        @CsvFileSource(resources = "/data/invalid-from-to-resource-path-parameters.csv", numLinesToSkip = 1)
        void shouldReturnBadRequest(String from, String to) throws Exception {
            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.MOVE_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_FROM_PATH_NAME, from)
                            .queryParam(ApiUtil.REQUEST_PARAM_TO_PATH_NAME, to))
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

    @Nested
    @DisplayName("Search for resources")
    class SearchResourcesTests {

        @ParameterizedTest
        @DisplayName("Search resources by query")
        @ArgumentsSource(SearchResourcesByQueryArgumentsProvider.class)
        void shouldFindResourcesInfo(String query, String expectedResponseJson) throws Exception {
            resourceManager.createDefaultResources();

            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            var actualResponseJson = mockMvc.perform(get(ApiUtil.SEARCH_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_QUERY_NAME, query))
                    .andExpect(status().isOk())
                    .andReturn()
                    .getResponse()
                    .getContentAsString();

            assertEquals(expectedResponseJson, actualResponseJson);
        }

        @ParameterizedTest
        @DisplayName("Invalid 'query' parameter")
        @CsvFileSource(resources = "/data/invalid-search-query-parameters.csv")
        void shouldReturnBadRequest(String query) throws Exception {
            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(get(ApiUtil.SEARCH_RESOURCE_URL)
                            .cookie(authSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_QUERY_NAME, query))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("User unauthorized")
        void shouldReturnUnauthorized() throws Exception {
            var guestSession = redisSessionManager.createGuestSession();
            var guestSessionInfo = redisSessionManager.getSessionInfo(guestSession);

            mockMvc.perform(get(ApiUtil.SEARCH_RESOURCE_URL)
                            .cookie(guestSessionInfo.cookie())
                            .queryParam(ApiUtil.REQUEST_PARAM_QUERY_NAME, "dummy"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
