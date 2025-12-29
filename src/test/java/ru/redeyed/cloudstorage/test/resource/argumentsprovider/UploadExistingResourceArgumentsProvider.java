package ru.redeyed.cloudstorage.test.resource.argumentsprovider;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.test.resource.ResourcePaths;
import ru.redeyed.cloudstorage.util.MockFileCreator;
import java.util.List;
import java.util.stream.Stream;

public class UploadExistingResourceArgumentsProvider implements ArgumentsProvider {

    @Override
    public @NotNull Stream<? extends Arguments> provideArguments(@NotNull ParameterDeclarations parameters,
                                                                 @NotNull ExtensionContext context) {
        return Stream.of(
                getUploadExistingInRootFileArguments(),
                getUploadExistingInRootFolderArguments(),
                getUploadExistingInDirectoryFileArguments(),
                getUploadExistingInDirectoryFolderArguments()
        );
    }

    private Arguments getUploadExistingInRootFileArguments() {
        var path = PathUtil.PATH_DELIMITER;
        var file = MockFileCreator.createDefault(ResourcePaths.FILE_1_TXT);
        return Arguments.of(path, List.of(file));
    }

    private Arguments getUploadExistingInRootFolderArguments() {
        var path = PathUtil.PATH_DELIMITER;
        var file = MockFileCreator.createDefault(ResourcePaths.FOLDER_1 + "non-existent-file.txt");
        return Arguments.of(path, List.of(file));
    }

    private Arguments getUploadExistingInDirectoryFileArguments() {
        var path = ResourcePaths.FOLDER_1;
        var file = MockFileCreator.createDefault(ResourcePaths.FILE_1_TXT);
        return Arguments.of(path, List.of(file));
    }

    private Arguments getUploadExistingInDirectoryFolderArguments() {
        var path = ResourcePaths.FOLDER_1;
        var filePath = PathUtil.removeRootParentDirectory(ResourcePaths.FOLDER_1_FOLDER_2 + "non-existent-file.txt");
        var file = MockFileCreator.createDefault(filePath);
        return Arguments.of(path, List.of(file));
    }
}
