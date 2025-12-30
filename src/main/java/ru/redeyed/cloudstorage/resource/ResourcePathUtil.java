package ru.redeyed.cloudstorage.resource;

import lombok.experimental.UtilityClass;
import ru.redeyed.cloudstorage.common.util.PathUtil;
import java.util.UUID;
import java.util.regex.Pattern;

@UtilityClass
public class ResourcePathUtil {

    private static final String USER_FILES_DIR_FORMAT = "user-%s-files/";

    private static final Pattern USER_FILES_DIR_PATTERN = Pattern.compile("^user-.*-files/");

    public static String createUserResourcePath(UUID userId) {
        return String.format(USER_FILES_DIR_FORMAT, userId);
    }

    public static String createUserResourcePath(UUID userId, String path) {
        if (PathUtil.isRootDirectory(path)) {
            path = PathUtil.trimLastSlash(path);
        }

        return String.format(USER_FILES_DIR_FORMAT, userId) + path;
    }

    public static String extractResourcePath(String path) {
        if (!USER_FILES_DIR_PATTERN.matcher(path).find()) {
            return PathUtil.removeResourceName(path);
        }

        path = PathUtil.removeResourceName(path);

        return removeUserFolder(path);
    }

    public static String removeUserFolder(String path) {
        if (USER_FILES_DIR_PATTERN.matcher(path).matches()) {
            return path.substring(path.length() - 1);
        }

        return path.replaceAll(USER_FILES_DIR_PATTERN.pattern(), "");
    }

    public static boolean hasUserFolder(String path) {
        return USER_FILES_DIR_PATTERN.matcher(path).find();
    }

    public static boolean isUserFolder(String path) {
        return USER_FILES_DIR_PATTERN.matcher(path).matches();
    }
}
