package ru.redeyed.cloudstorage.test.resource.argumentsprovider.directory;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.test.resource.ResourceManager;
import ru.redeyed.cloudstorage.test.resource.ResourcePaths;
import java.util.List;
import java.util.stream.Stream;

public class MoveDirectoryArgumentsProvider implements ArgumentsProvider {

    @Override
    public @NotNull Stream<? extends Arguments> provideArguments(@NotNull ParameterDeclarations parameters,
                                                                 @NotNull ExtensionContext context) {
        return Stream.of(
                getFolderOneArguments(),
                getFolderTwoArguments()
        );
    }

    private Arguments getFolderOneArguments() {
        var from = ResourcePaths.FOLDER_1;
        var to = ResourcePaths.FOLDER_4 + from;
        var expectedPath = PathUtil.removeResourceName(to);
        var expectedName = PathUtil.extractResourceName(from);

        var expectedExistResourcePaths = List.of(
                ResourceManager.USER_FILES_DIR_PATH + to,
                ResourceManager.USER_FILES_DIR_PATH + ResourcePaths.FOLDER_4 + ResourcePaths.FOLDER_1_FILE_2_TXT,
                ResourceManager.USER_FILES_DIR_PATH + ResourcePaths.FOLDER_4 + ResourcePaths.FOLDER_1_FOLDER_2,
                ResourceManager.USER_FILES_DIR_PATH + ResourcePaths.FOLDER_4 + ResourcePaths.FOLDER_1_FOLDER_2_FILE_3_TXT,
                ResourceManager.USER_FILES_DIR_PATH + ResourcePaths.FOLDER_4 + ResourcePaths.FOLDER_1_FOLDER_2_FOLDER_3
        );

        return Arguments.of(from, to, expectedPath, expectedName, expectedExistResourcePaths);
    }

    private Arguments getFolderTwoArguments() {
        var from = ResourcePaths.FOLDER_1_FOLDER_2;
        var to = PathUtil.removeRootParentDirectory(from);
        var expectedPath = PathUtil.PATH_DELIMITER;
        var expectedName = PathUtil.extractResourceName(from);

        var expectedExistResourcePaths = List.of(
                ResourceManager.USER_FILES_DIR_PATH + to,
                ResourceManager.USER_FILES_DIR_PATH + PathUtil.removeRootParentDirectory(ResourcePaths.FOLDER_1_FOLDER_2_FILE_3_TXT),
                ResourceManager.USER_FILES_DIR_PATH + PathUtil.removeRootParentDirectory(ResourcePaths.FOLDER_1_FOLDER_2_FOLDER_3)
        );

        return Arguments.of(from, to, expectedPath, expectedName, expectedExistResourcePaths);
    }
}
