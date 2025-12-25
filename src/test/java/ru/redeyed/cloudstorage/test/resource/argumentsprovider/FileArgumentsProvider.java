package ru.redeyed.cloudstorage.test.resource.argumentsprovider;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;
import ru.redeyed.cloudstorage.util.MockFileCreator;
import java.util.stream.Stream;

public class FileArgumentsProvider implements ArgumentsProvider {

    @Override
    public @NotNull Stream<? extends Arguments> provideArguments(@NotNull ParameterDeclarations parameters,
                                                                 @NotNull ExtensionContext context) {
        return Stream.of(
                createArguments("test-file", MockFileCreator.EMPTY_CONTENT),
                createArguments("test-file.txt", MockFileCreator.TEST_TEXT_CONTENT),
                createArguments("folder1/test-file.txt", MockFileCreator.TEST_TEXT_CONTENT),
                createArguments("folder1/folder2/test-file.txt", MockFileCreator.TEST_TEXT_CONTENT)
        );
    }

    private Arguments createArguments(String filePath, byte[] content) {
        var file = MockFileCreator.create(filePath, content);
        return Arguments.of(file, filePath);
    }
}
