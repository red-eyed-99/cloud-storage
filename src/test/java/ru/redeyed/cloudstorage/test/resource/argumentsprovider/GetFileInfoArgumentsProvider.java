package ru.redeyed.cloudstorage.test.resource.argumentsprovider;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;
import ru.redeyed.cloudstorage.util.MockFileCreator;
import java.util.stream.Stream;

public class GetFileInfoArgumentsProvider implements ArgumentsProvider {

    @Override
    public @NotNull Stream<? extends Arguments> provideArguments(@NotNull ParameterDeclarations parameters,
                                                                 @NotNull ExtensionContext context) {
        return Stream.of(
                createArguments("test-file"),
                createArguments("test-file.txt"),
                createArguments("folder1/test-file.txt"),
                createArguments("folder1/folder2/test-file.txt")
        );
    }

    private Arguments createArguments(String filePath) {
        var file = MockFileCreator.create(filePath);
        return Arguments.of(file, filePath);
    }
}
