package ru.redeyed.cloudstorage.test.resource;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.resource.ResourcePathUtil;
import ru.redeyed.cloudstorage.resource.ResourceType;
import ru.redeyed.cloudstorage.s3.BucketName;
import ru.redeyed.cloudstorage.s3.SimpleStorageService;
import ru.redeyed.cloudstorage.test.auth.session.RedisSessionManager;
import ru.redeyed.cloudstorage.test.integration.BaseIntegrationTest;
import ru.redeyed.cloudstorage.test.resource.argumentsprovider.GetFileInfoArgumentsProvider;
import ru.redeyed.cloudstorage.test.user.UserTestData;
import ru.redeyed.cloudstorage.util.ApiUtil;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Interaction with resources")
@RequiredArgsConstructor
public class ResourceIntegrationTest extends BaseIntegrationTest {

    private final MockMvc mockMvc;

    private final RedisSessionManager redisSessionManager;

    private final SimpleStorageService storageService;

    @ParameterizedTest(name = "{1}")
    @DisplayName("Get information about file")
    @ArgumentsSource(GetFileInfoArgumentsProvider.class)
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

        mockMvc.perform(get(ApiUtil.GET_RESOURCE_INFO_URL)
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

    @ParameterizedTest(name = "{0}")
    @DisplayName("Get information about directory")
    @CsvSource({
            "test-directory/",
            "test-directory-1/test-directory-2/",
            "test-directory-1/test-directory-2/test-directory-3/"
    })
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

        mockMvc.perform(get(ApiUtil.GET_RESOURCE_INFO_URL)
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
}
