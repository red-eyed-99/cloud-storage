package ru.redeyed.cloudstorage.test.resource.argumentsprovider;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.test.resource.ResourcePaths;
import java.util.stream.Stream;

public class ResourceAlreadyExistsArgumentsProvider implements ArgumentsProvider {

    @Override
    public @NotNull Stream<? extends Arguments> provideArguments(@NotNull ParameterDeclarations parameters,
                                                                 @NotNull ExtensionContext context) {
        return Stream.of(
                Arguments.of(ResourcePaths.UNDEFINED_FILE_1, ResourcePaths.UNDEFINED_FILE_1),
                Arguments.of(ResourcePaths.UNDEFINED_FILE_1, ResourcePaths.FILE_1_TXT),
                getDirectoryWithFileNameExistsArguments(),
                getFileWithDirectoryNameExistsArguments(),
                getMoveDirectoryToItselfArguments(),
                Arguments.of(ResourcePaths.FOLDER_1, ResourcePaths.FOLDER_1),
                Arguments.of(ResourcePaths.FOLDER_1, ResourcePaths.FOLDER_4),
                Arguments.of(ResourcePaths.FILE_1_TXT, ResourcePaths.FOLDER_1_FILE_1_TXT),
                Arguments.of(ResourcePaths.FOLDER_1, ResourcePaths.FOLDER_5_FOLDER_1)
        );
    }

    private Arguments getDirectoryWithFileNameExistsArguments() {
        var fromPath = ResourcePaths.UNDEFINED_FILE_1;
        var toPath = ResourcePaths.FOLDER_1 + ResourcePaths.UNDEFINED_FILE_1;
        return Arguments.of(fromPath, toPath);
    }

    private Arguments getFileWithDirectoryNameExistsArguments() {
        var fromPath = ResourcePaths.FOLDER_1_UNDEFINED_FILE_1_FOLDER;
        var toPath = PathUtil.removeRootParentDirectory(ResourcePaths.FOLDER_1_UNDEFINED_FILE_1_FOLDER);
        return Arguments.of(fromPath, toPath);
    }

    private Arguments getMoveDirectoryToItselfArguments() {
        var fromPath = ResourcePaths.FOLDER_1;
        var toPath = ResourcePaths.FOLDER_1 + ResourcePaths.FOLDER_1;
        return Arguments.of(fromPath, toPath);
    }
}
