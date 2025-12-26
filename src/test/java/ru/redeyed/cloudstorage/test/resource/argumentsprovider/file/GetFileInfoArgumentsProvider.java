package ru.redeyed.cloudstorage.test.resource.argumentsprovider.file;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.test.resource.ResourcePaths;
import ru.redeyed.cloudstorage.util.MockFileCreator;
import java.util.stream.Stream;

public class GetFileInfoArgumentsProvider implements ArgumentsProvider {

    @Override
    public @NotNull Stream<? extends Arguments> provideArguments(@NotNull ParameterDeclarations parameters,
                                                                 @NotNull ExtensionContext context) {
        return Stream.of(
                getUndefinedFileOneArguments(),
                getFileOneTxtArguments(),
                getFileTwoTxtArguments(),
                getFileThreeTxtArguments()
        );
    }

    private Arguments getUndefinedFileOneArguments() {
        var path = ResourcePaths.UNDEFINED_FILE_1;
        var expectedPath = PathUtil.PATH_DELIMITER;
        var expectedName = ResourcePaths.UNDEFINED_FILE_1;
        var expectedSize = MockFileCreator.DEFAULT_CONTENT_SIZE_BYTES;

        return Arguments.of(path, expectedPath, expectedName, expectedSize);
    }

    private Arguments getFileOneTxtArguments() {
        var path = ResourcePaths.FILE_1_TXT;
        var expectedPath = PathUtil.PATH_DELIMITER;
        var expectedName = ResourcePaths.FILE_1_TXT;
        var expectedSize = MockFileCreator.DEFAULT_TEXT_CONTENT_SIZE_BYTES;

        return Arguments.of(path, expectedPath, expectedName, expectedSize);
    }

    private Arguments getFileTwoTxtArguments() {
        var path = ResourcePaths.FOLDER_1_FILE_2_TXT;
        var expectedPath = PathUtil.removeResourceName(path);
        var expectedName = PathUtil.extractResourceName(path);
        var expectedSize = MockFileCreator.DEFAULT_TEXT_CONTENT_SIZE_BYTES;

        return Arguments.of(path, expectedPath, expectedName, expectedSize);
    }

    private Arguments getFileThreeTxtArguments() {
        var path = ResourcePaths.FOLDER_1_FOLDER_2_FILE_3_TXT;
        var expectedPath = PathUtil.removeResourceName(path);
        var expectedName = PathUtil.extractResourceName(path);
        var expectedSize = MockFileCreator.DEFAULT_TEXT_CONTENT_SIZE_BYTES;

        return Arguments.of(path, expectedPath, expectedName, expectedSize);
    }
}
