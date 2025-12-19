package ru.redeyed.cloudstorage.common.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PathUtil {

    public static final String PATH_DELIMITER = "/";

    private static final int CHARACTER_NOT_PRESENT = -1;

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

    public static boolean isRootDirectory(String path) {
        return path.equals(PATH_DELIMITER);
    }

    public static boolean isDirectory(String path) {
        return path.endsWith(PATH_DELIMITER);
    }

    private static String extractDirectoryName(String path) {
        var beforeLastDelimiterIndex = path.length() - 2;
        var penultimateDelimiterIndex = path.lastIndexOf(PATH_DELIMITER, beforeLastDelimiterIndex);

        var beginIndex = 0;
        var endIndex = beforeLastDelimiterIndex + 1;

        if (penultimateDelimiterIndex == CHARACTER_NOT_PRESENT) {
            return path.substring(beginIndex, endIndex);
        }

        beginIndex = penultimateDelimiterIndex + 1;
        endIndex = beforeLastDelimiterIndex + 1;

        return path.substring(beginIndex, endIndex);
    }

    private static String removeDirectoryName(String path) {
        var beforeLastDelimiterIndex = path.length() - 2;
        var penultimateDelimiterIndex = path.lastIndexOf(PATH_DELIMITER, beforeLastDelimiterIndex);

        if (penultimateDelimiterIndex == CHARACTER_NOT_PRESENT) {
            return PATH_DELIMITER;
        }

        var beginIndex = 0;
        var endIndex = penultimateDelimiterIndex + 1;

        return path.substring(beginIndex, endIndex);
    }

    private static String extractFileName(String path) {
        var lastDelimiterIndex = path.lastIndexOf(PATH_DELIMITER);

        if (lastDelimiterIndex == CHARACTER_NOT_PRESENT) {
            return path;
        }

        var beginIndex = lastDelimiterIndex + 1;

        return path.substring(beginIndex);
    }

    private static String removeFileName(String path) {
        var lastDelimiterIndex = path.lastIndexOf(PATH_DELIMITER);

        if (lastDelimiterIndex == CHARACTER_NOT_PRESENT) {
            return PATH_DELIMITER;
        }

        var beginIndex = 0;
        var endIndex = lastDelimiterIndex + 1;

        return path.substring(beginIndex, endIndex);
    }

    public static String trimLastSlash(String path) {
        var beginIndex = 0;
        var endIndex = path.length() - 1;
        return path.substring(beginIndex, endIndex);
    }
}
