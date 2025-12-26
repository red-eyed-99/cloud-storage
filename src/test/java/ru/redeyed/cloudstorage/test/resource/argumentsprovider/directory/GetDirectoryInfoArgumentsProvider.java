package ru.redeyed.cloudstorage.test.resource.argumentsprovider.directory;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.test.resource.ResourcePaths;
import java.util.stream.Stream;

public class GetDirectoryInfoArgumentsProvider implements ArgumentsProvider {

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
        var expectedPath = PathUtil.PATH_DELIMITER;
        var expectedName = PathUtil.extractResourceName(path);

        return Arguments.of(path, expectedPath, expectedName);
    }

    private Arguments getFolderTwoArguments() {
        var path = ResourcePaths.FOLDER_1_FOLDER_2;
        var expectedPath = PathUtil.removeResourceName(path);
        var expectedName = PathUtil.extractResourceName(path);

        return Arguments.of(path, expectedPath, expectedName);
    }

    private Arguments getFolderThreeArguments() {
        var path = ResourcePaths.FOLDER_1_FOLDER_2_FOLDER_3;
        var expectedPath = PathUtil.removeResourceName(path);
        var expectedName = PathUtil.extractResourceName(path);

        return Arguments.of(path, expectedPath, expectedName);
    }

    private Arguments getFolderFourArguments() {
        var path = ResourcePaths.FOLDER_4;
        var expectedPath = PathUtil.PATH_DELIMITER;
        var expectedName = PathUtil.extractResourceName(path);

        return Arguments.of(path, expectedPath, expectedName);
    }
}
