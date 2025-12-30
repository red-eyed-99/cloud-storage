package ru.redeyed.cloudstorage.test.resource.argumentsprovider;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.springframework.mock.web.MockMultipartFile;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.resource.ResourceType;
import ru.redeyed.cloudstorage.resource.dto.ResourceResponseDto;
import ru.redeyed.cloudstorage.resource.validation.validator.ResourcePathValidator;
import ru.redeyed.cloudstorage.test.resource.ResourceManager;
import ru.redeyed.cloudstorage.test.resource.ResourcePaths;
import ru.redeyed.cloudstorage.util.JsonUtil;
import ru.redeyed.cloudstorage.util.MockFileCreator;
import tools.jackson.core.type.TypeReference;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class UploadInvalidResourceArgumentsProvider implements ArgumentsProvider {

    @Override
    public @NotNull Stream<? extends Arguments> provideArguments(@NotNull ParameterDeclarations parameters,
                                                                 @NotNull ExtensionContext context) {
        return Stream.of(
                getFilesRootDirectoryNotTheSameArguments(),
                getFilenameLengthExceedArguments(),
                getFilenameBytesExceedArguments(),
                getFilePathStartsWithDelimiterArguments(),
                getFilePathBytesExceedArguments()
        );
    }

    private Arguments getFilesRootDirectoryNotTheSameArguments() {
        var description = "root parent folder of the files is different";

        var files = new ArrayList<MockMultipartFile>();

        var firstFile = MockFileCreator.createDefault("parent-folder-1/file-1.txt");
        var secondFile = MockFileCreator.createDefault("parent-folder-2/file-2.txt");

        files.add(firstFile);
        files.add(secondFile);

        return Arguments.of(files, description);
    }

    private Arguments getFilenameLengthExceedArguments() {
        var maxLength = ResourcePathValidator.RESOURCE_NAME_MAX_LENGTH;

        var description = "filename length exceeds the maximum allowed length (" + maxLength + ")";

        var symbol = "a";

        var fileName = symbol.repeat(maxLength + 1);

        var file = MockFileCreator.createDefault(fileName);

        return Arguments.of(
                List.of(file), description
        );
    }

    private Arguments getFilenameBytesExceedArguments() {
        var maxBytes = ResourcePathValidator.RESOURCE_NAME_MAX_BYTES;

        var description = "filename bytes exceeds the maximum allowed size (" + maxBytes + ")";

        var symbol = "💩";
        var maxAllowedSymbolCount = maxBytes / symbol.getBytes().length;

        var fileName = symbol.repeat(maxAllowedSymbolCount + 1);

        var file = MockFileCreator.createDefault(fileName);

        return Arguments.of(
                List.of(file), description
        );
    }

    private Arguments getFilePathStartsWithDelimiterArguments() {
        var description = "path starts with '" + PathUtil.PATH_DELIMITER + "'";

        var file = MockFileCreator.createDefault("/folder/file-1.txt");

        return Arguments.of(
                List.of(file), description
        );
    }

    private Arguments getFilePathBytesExceedArguments() {
        var maxBytes = ResourcePathValidator.PATH_MAX_BYTES;

        var description = "filepath bytes exceeds the maximum allowed size (" + maxBytes + ")";

        var symbol = "💩";
        var maxAllowedSymbolCount = maxBytes / symbol.getBytes().length;

        var fileName = symbol.repeat(maxAllowedSymbolCount + 1);

        var file = MockFileCreator.createDefault(fileName);

        return Arguments.of(
                List.of(file), description
        );
    }

    private Arguments getFilePathHasExtraSpacesArguments() {
        var description = "path starts with '" + PathUtil.PATH_DELIMITER + "'";

        var file = MockFileCreator.createDefault("/folder/file-1.txt");

        return Arguments.of(
                List.of(file), description
        );
    }
}
