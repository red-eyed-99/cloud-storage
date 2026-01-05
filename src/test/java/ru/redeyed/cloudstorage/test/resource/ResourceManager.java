package ru.redeyed.cloudstorage.test.resource;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.resource.ResourcePathUtil;
import ru.redeyed.cloudstorage.s3.BucketName;
import ru.redeyed.cloudstorage.s3.SimpleStorageService;
import ru.redeyed.cloudstorage.test.user.UserTestData;
import ru.redeyed.cloudstorage.util.MockFileCreator;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ResourceManager {

    public static final String USER_FILES_DIR_PATH = ResourcePathUtil.createUserResourcePath(UserTestData.ID);

    private static final List<String> DEFAULT_RESOURCE_STRUCTURE = List.of(
            ResourcePaths.UNDEFINED_FILE_1,
            ResourcePaths.FILE_1_TXT,
            ResourcePaths.FOLDER_1,
            ResourcePaths.FOLDER_1_FILE_1_TXT,
            ResourcePaths.FOLDER_1_FILE_2_TXT,
            ResourcePaths.FOLDER_1_UNDEFINED_FILE_1_FOLDER,
            ResourcePaths.FOLDER_1_FOLDER_2,
            ResourcePaths.FOLDER_1_FOLDER_2_FILE_3_TXT,
            ResourcePaths.FOLDER_1_FOLDER_2_FOLDER_3,
            ResourcePaths.FOLDER_4,
            ResourcePaths.FOLDER_5,
            ResourcePaths.FOLDER_5_FOLDER_1
    );

    private final SimpleStorageService storageService;

    public void createDefaultResources() {
        var files = new ArrayList<MultipartFile>();

        for (var resourcePath : DEFAULT_RESOURCE_STRUCTURE) {
            if (PathUtil.isDirectory(resourcePath)) {
                storageService.createDirectory(BucketName.USER_FILES, USER_FILES_DIR_PATH + resourcePath);
                continue;
            }

            var file = MockFileCreator.createDefault(resourcePath);

            files.add(file);
        }

        storageService.uploadFiles(BucketName.USER_FILES, USER_FILES_DIR_PATH, files);
    }

    public void clearResources() {
        storageService.removeDirectory(BucketName.USER_FILES, USER_FILES_DIR_PATH);
    }
}
