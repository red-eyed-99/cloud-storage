package ru.redeyed.cloudstorage.test.resource.argumentsprovider.file;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;
import ru.redeyed.cloudstorage.test.resource.ResourcePaths;
import ru.redeyed.cloudstorage.util.MockFileCreator;
import java.util.stream.Stream;

public class DownloadFileArgumentsProvider implements ArgumentsProvider {

    @Override
    public @NotNull Stream<? extends Arguments> provideArguments(@NotNull ParameterDeclarations parameters,
                                                                 @NotNull ExtensionContext context) {
        return Stream.of(
                Arguments.of(ResourcePaths.UNDEFINED_FILE_1, MockFileCreator.DEFAULT_CONTENT),
                Arguments.of(ResourcePaths.FILE_1_TXT, MockFileCreator.DEFAULT_TEXT_CONTENT),
                Arguments.of(ResourcePaths.FOLDER_1_FILE_2_TXT, MockFileCreator.DEFAULT_TEXT_CONTENT),
                Arguments.of(ResourcePaths.FOLDER_1_FOLDER_2_FILE_3_TXT, MockFileCreator.DEFAULT_TEXT_CONTENT)
        );
    }
}
