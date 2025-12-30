package ru.redeyed.cloudstorage.test.resource.argumentsprovider.directory;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.test.resource.ResourcePaths;
import ru.redeyed.cloudstorage.util.JsonUtil;
import java.util.stream.Stream;

public class GetDirectoryContentArgumentsProvider implements ArgumentsProvider {

    @Override
    public @NotNull Stream<? extends Arguments> provideArguments(@NotNull ParameterDeclarations parameters,
                                                                 @NotNull ExtensionContext context) {
        return Stream.of(
                getRootDirectoryContentArguments(),
                getFolderOneContentArguments(),
                getFolderTwoContentArguments(),
                getFolderFourContentArguments()
        );
    }

    private Arguments getRootDirectoryContentArguments() {
        var path = PathUtil.PATH_DELIMITER;
        var expectedResponseJson = JsonUtil.getJsonFrom("data/responses/directory_content/get-root-directory-content.json");
        return Arguments.of(path, expectedResponseJson);
    }

    private Arguments getFolderOneContentArguments() {
        var path = ResourcePaths.FOLDER_1;
        var expectedResponseJson = JsonUtil.getJsonFrom("data/responses/directory_content/get-folder-1-content.json");
        return Arguments.of(path, expectedResponseJson);
    }

    private Arguments getFolderTwoContentArguments() {
        var path = ResourcePaths.FOLDER_1_FOLDER_2;
        var expectedResponseJson = JsonUtil.getJsonFrom("data/responses/directory_content/get-folder-2-content.json");
        return Arguments.of(path, expectedResponseJson);
    }

    private Arguments getFolderFourContentArguments() {
        var path = ResourcePaths.FOLDER_4;
        var expectedResponseJson = JsonUtil.getJsonFrom("data/responses/directory_content/get-folder-4-content.json");
        return Arguments.of(path, expectedResponseJson);
    }
}