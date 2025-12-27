package ru.redeyed.cloudstorage.test.resource.argumentsprovider;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import ru.redeyed.cloudstorage.common.util.StringUtil;
import ru.redeyed.cloudstorage.test.resource.ResourcePaths;
import ru.redeyed.cloudstorage.util.JsonUtil;
import java.util.HashMap;
import java.util.stream.Stream;

public class SearchResourcesByQueryArgumentsProvider implements ArgumentsProvider {

    @Override
    public @NotNull Stream<? extends Arguments> provideArguments(@NotNull ParameterDeclarations parameters,
                                                                 @NotNull ExtensionContext context) {
        return Stream.of(
                searchAnyFileWithExtensionArguments(),
                searchFileOneTxtArguments(),
                searchFolderTwoArguments(),
                searchAnyDirectoryStartsWithFolderArguments()
        );
    }

    private Arguments searchAnyFileWithExtensionArguments() {
        var query = ".";
        var expectedResponseJson = JsonUtil.getJsonFrom("data/responses/search/search-any-file-with-extension.json");
        return Arguments.of(query, removeUnnecessaryCharacters(expectedResponseJson));
    }

    private Arguments searchFileOneTxtArguments() {
        var expectedResponseJson = JsonUtil.getJsonFrom("data/responses/search/search-file-1-txt.json");
        return Arguments.of(ResourcePaths.FILE_1_TXT, removeUnnecessaryCharacters(expectedResponseJson));
    }

    private Arguments searchFolderTwoArguments() {
        var query = PathUtil.extractResourceName(ResourcePaths.FOLDER_1_FOLDER_2);
        var expectedResponseJson = JsonUtil.getJsonFrom("data/responses/search/search-folder-2.json");
        return Arguments.of(query, removeUnnecessaryCharacters(expectedResponseJson));
    }

    private Arguments searchAnyDirectoryStartsWithFolderArguments() {
        var query = "folder";
        var expectedResponseJson = JsonUtil.getJsonFrom("data/responses/search/search-any-directory-starts-with-folder.json");
        return Arguments.of(query, removeUnnecessaryCharacters(expectedResponseJson));
    }

    private String removeUnnecessaryCharacters(String json) {
        var regexesReplacements = new HashMap<String, String>();

        regexesReplacements.put("[\n\r]", "");
        regexesReplacements.put(": ", ":");
        regexesReplacements.put("\\s{2,}", "");

        return StringUtil.removeUnnecessaryCharacters(json, regexesReplacements);
    }
}