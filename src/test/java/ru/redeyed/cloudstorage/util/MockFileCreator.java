package ru.redeyed.cloudstorage.util;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.mock.web.MockMultipartFile;
import ru.redeyed.cloudstorage.common.util.PathUtil;

@UtilityClass
public class MockFileCreator {

    private static final byte[] EMPTY_CONTENT = new byte[0];

    public static MockMultipartFile create(String filePath) {
        var fileExtension = PathUtil.extractFileExtension(filePath);

        return switch (fileExtension) {
            case "" -> createUndefinedFile(filePath);
            case "txt" -> createTextFile(filePath);
            default -> throw new IllegalArgumentException("Unknown file extension: " + fileExtension);
        };
    }

    private static MockMultipartFile createUndefinedFile(String fullPath) {
        return new MockMultipartFile(
                ApiUtil.REQUEST_PART_FILES_NAME,
                fullPath,
                null,
                EMPTY_CONTENT
        );
    }

    private static MockMultipartFile createTextFile(String fullPath) {
        return new MockMultipartFile(
                ApiUtil.REQUEST_PART_FILES_NAME,
                fullPath,
                ContentType.TEXT_PLAIN.toString(),
                EMPTY_CONTENT
        );
    }
}
