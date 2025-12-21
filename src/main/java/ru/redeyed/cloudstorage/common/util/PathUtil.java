package ru.redeyed.cloudstorage.common.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PathUtil {

    public static final String PATH_DELIMITER = "/";

    private static final int CHARACTER_NOT_PRESENT = -1;

    public static String extractPathFrom(String value, String path) {
        var startIndex = path.indexOf(value);
        return path.substring(startIndex);
    }

    public static String extractResourceName(String path) {
        return isDirectory(path)
                ? extractDirectoryName(path)
                : extractFileName(path);
    }

    public static String removeResourceName(String path) {
        return isDirectory(path)
                ? removeDirectoryName(path)
                : removeFileName(path);
    }

    public static String removeParentDirectory(String path) {
        var parentDirectoryEndIndex = path.indexOf(PATH_DELIMITER);
        return path.substring(parentDirectoryEndIndex + PATH_DELIMITER.length());
    }

    public static boolean isRootDirectory(String path) {
        return path.equals(PATH_DELIMITER);
    }

    public static boolean isDirectory(String path) {
        return path.endsWith(PATH_DELIMITER);
    }

    private static String extractDirectoryName(String path) {
        var beforeLastDelimiterIndex = path.length() - PATH_DELIMITER.length() - 1;
        var penultimateDelimiterIndex = path.lastIndexOf(PATH_DELIMITER, beforeLastDelimiterIndex);

        var beginIndex = 0;
        var endIndex = beforeLastDelimiterIndex + PATH_DELIMITER.length();

        if (penultimateDelimiterIndex == CHARACTER_NOT_PRESENT) {
            return path.substring(beginIndex, endIndex);
        }

        beginIndex = penultimateDelimiterIndex + PATH_DELIMITER.length();
        endIndex = beforeLastDelimiterIndex + PATH_DELIMITER.length();

        return path.substring(beginIndex, endIndex);
    }

    private static String removeDirectoryName(String path) {
        var beforeLastDelimiterIndex = path.length() - PATH_DELIMITER.length() - 1;
        var penultimateDelimiterIndex = path.lastIndexOf(PATH_DELIMITER, beforeLastDelimiterIndex);

        if (penultimateDelimiterIndex == CHARACTER_NOT_PRESENT) {
            return PATH_DELIMITER;
        }

        var beginIndex = 0;
        var endIndex = penultimateDelimiterIndex + PATH_DELIMITER.length();

        return path.substring(beginIndex, endIndex);
    }

    private static String extractFileName(String path) {
        var lastDelimiterIndex = path.lastIndexOf(PATH_DELIMITER);

        if (lastDelimiterIndex == CHARACTER_NOT_PRESENT) {
            return path;
        }

        var beginIndex = lastDelimiterIndex + PATH_DELIMITER.length();

        return path.substring(beginIndex);
    }

    private static String removeFileName(String path) {
        var lastDelimiterIndex = path.lastIndexOf(PATH_DELIMITER);

        if (lastDelimiterIndex == CHARACTER_NOT_PRESENT) {
            return PATH_DELIMITER;
        }

        var beginIndex = 0;
        var endIndex = lastDelimiterIndex + PATH_DELIMITER.length();

        return path.substring(beginIndex, endIndex);
    }

    public static String trimLastSlash(String path) {
        var beginIndex = 0;
        var endIndex = path.length() - PATH_DELIMITER.length();
        return path.substring(beginIndex, endIndex);
    }
}
