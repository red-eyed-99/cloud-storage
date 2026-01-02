package ru.redeyed.cloudstorage.test.resource.argumentsprovider.directory;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.test.resource.ResourcePaths;
import java.util.List;
import java.util.stream.Stream;

public class DownloadDirectoryArgumentsProvider implements ArgumentsProvider {

    @Override
    public @NotNull Stream<? extends Arguments> provideArguments(@NotNull ParameterDeclarations parameters,
                                                                 @NotNull ExtensionContext context) {
        return Stream.of(
                getFolderOneArguments(),
                getFolderTwoArguments(),
                getFolderThreeArguments(),
                getFolderFourArguments()
        );
    }

    private Arguments getFolderOneArguments() {
        var path = ResourcePaths.FOLDER_1;

        var resourcePaths = List.of(
                ResourcePaths.FOLDER_1,
                ResourcePaths.FOLDER_1_FILE_1_TXT,
                ResourcePaths.FOLDER_1_FILE_2_TXT,
                ResourcePaths.FOLDER_1_FOLDER_2,
                ResourcePaths.FOLDER_1_FOLDER_2_FILE_3_TXT,
                ResourcePaths.FOLDER_1_FOLDER_2_FOLDER_3
        );

        return Arguments.of(path, resourcePaths);
    }

    private Arguments getFolderTwoArguments() {
        var path = ResourcePaths.FOLDER_1_FOLDER_2;

        var resourcePaths = List.of(
                PathUtil.removeRootParentDirectory(ResourcePaths.FOLDER_1_FOLDER_2),
                PathUtil.removeRootParentDirectory(ResourcePaths.FOLDER_1_FOLDER_2_FILE_3_TXT),
                PathUtil.removeRootParentDirectory(ResourcePaths.FOLDER_1_FOLDER_2_FOLDER_3)
        );

        return Arguments.of(path, resourcePaths);
    }

    private Arguments getFolderThreeArguments() {
        var path = ResourcePaths.FOLDER_1_FOLDER_2_FOLDER_3;
        var directoryPath = PathUtil.extractResourceName(path) + PathUtil.PATH_DELIMITER;
        var resourcePaths = List.of(directoryPath);
        return Arguments.of(path, resourcePaths);
    }

    private Arguments getFolderFourArguments() {
        var path = ResourcePaths.FOLDER_4;
        var resourcePaths = List.of(ResourcePaths.FOLDER_4);
        return Arguments.of(path, resourcePaths);
    }
}
