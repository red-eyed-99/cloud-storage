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

public class MoveFileArgumentsProvider implements ArgumentsProvider {

    @Override
    public @NotNull Stream<? extends Arguments> provideArguments(@NotNull ParameterDeclarations parameters,
                                                                 @NotNull ExtensionContext context) {
        return Stream.of(
                getFileOneTxtArguments(),
                getFileTwoTxtArguments(),
                getFileThreeTxtArguments()
        );
    }

    private Arguments getFileOneTxtArguments() {
        var from = ResourcePaths.FILE_1_TXT;
        var fileName = PathUtil.extractResourceName(from);
        var to = ResourcePaths.FOLDER_4 + fileName;
        var expectedPath = PathUtil.removeResourceName(to);
        var expectedSize = MockFileCreator.DEFAULT_TEXT_CONTENT_SIZE_BYTES;

        return Arguments.of(from, to, expectedPath, fileName, expectedSize);
    }

    private Arguments getFileTwoTxtArguments() {
        var from = ResourcePaths.FOLDER_1_FILE_2_TXT;
        var fileName = PathUtil.extractResourceName(from);
        var to = ResourcePaths.FOLDER_1_FOLDER_2 + fileName;
        var expectedPath = PathUtil.removeResourceName(to);
        var expectedSize = MockFileCreator.DEFAULT_TEXT_CONTENT_SIZE_BYTES;

        return Arguments.of(from, to, expectedPath, fileName, expectedSize);
    }

    private Arguments getFileThreeTxtArguments() {
        var from = ResourcePaths.FOLDER_1_FOLDER_2_FILE_3_TXT;
        var fileName = PathUtil.extractResourceName(from);
        var expectedSize = MockFileCreator.DEFAULT_TEXT_CONTENT_SIZE_BYTES;

        return Arguments.of(from, fileName, PathUtil.PATH_DELIMITER, fileName, expectedSize);
    }
}
