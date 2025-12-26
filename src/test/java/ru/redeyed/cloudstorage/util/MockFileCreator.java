package ru.redeyed.cloudstorage.util;

import com.github.dockerjava.zerodep.shaded.org.apache.hc.core5.http.ContentType;
import lombok.experimental.UtilityClass;
import org.springframework.mock.web.MockMultipartFile;
import ru.redeyed.cloudstorage.common.util.PathUtil;

@UtilityClass
public class MockFileCreator {

    public static final byte[] DEFAULT_CONTENT = new byte[]{1, 2, 3};
    public static final long DEFAULT_CONTENT_SIZE_BYTES = 3;

    public static final byte[] DEFAULT_TEXT_CONTENT = "Test text".getBytes();
    public static final long DEFAULT_TEXT_CONTENT_SIZE_BYTES = 9;

    public static MockMultipartFile createDefault(String filePath) {
        var fileExtension = PathUtil.extractFileExtension(filePath);

        var content = switch (fileExtension) {
            case UNDEFINED -> DEFAULT_CONTENT;
            case TXT -> DEFAULT_TEXT_CONTENT;
        };

        return createFile(filePath, content);
    }

    private static MockMultipartFile createFile(String filePath, byte[] content) {
        var fileExtension = PathUtil.extractFileExtension(filePath);

        return switch (fileExtension) {
            case UNDEFINED -> createUndefinedFile(filePath, content);
            case TXT -> createTextFile(filePath, content);
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
