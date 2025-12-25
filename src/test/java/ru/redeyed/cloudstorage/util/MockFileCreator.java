package ru.redeyed.cloudstorage.util;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.mock.web.MockMultipartFile;
import ru.redeyed.cloudstorage.common.util.PathUtil;

@UtilityClass
public class MockFileCreator {

    public static final byte[] EMPTY_CONTENT = new byte[0];

    public static final byte[] TEST_TEXT_CONTENT = "Test text".getBytes();

    public static MockMultipartFile create(String filePath, byte[] content) {
        var fileExtension = PathUtil.extractFileExtension(filePath);

        return switch (fileExtension) {
            case "" -> createUndefinedFile(filePath, content);
            case "txt" -> createTextFile(filePath, content);
            default -> throw new IllegalArgumentException("Unknown file extension: " + fileExtension);
        };
    }

    private static MockMultipartFile createUndefinedFile(String fullPath, byte[] content) {
        return new MockMultipartFile(
                ApiUtil.REQUEST_PART_FILES_NAME,
                fullPath,
                null,
                content
        );
    }

    private static MockMultipartFile createTextFile(String fullPath, byte[] content) {
        return new MockMultipartFile(
                ApiUtil.REQUEST_PART_FILES_NAME,
                fullPath,
                ContentType.TEXT_PLAIN.toString(),
                content
        );
    }
}
