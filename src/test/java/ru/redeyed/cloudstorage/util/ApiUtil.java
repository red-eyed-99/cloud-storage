package ru.redeyed.cloudstorage.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiUtil {

    public static final String SIGN_IN_URL = "/api/auth/sign-in";
    public static final String SIGN_UP_URL = "/api/auth/sign-up";
    public static final String SIGN_OUT_URL = "/api/auth/sign-out";

    public static final String RESOURCE_URL = "/api/resource";
    public static final String DOWNLOAD_RESOURCE_URL = "/api/resource/download";
    public static final String MOVE_RESOURCE_URL = "/api/resource/move";

    public static final String REQUEST_PARAM_PATH_NAME = "path";
    public static final String REQUEST_PARAM_FROM_PATH_NAME = "from";
    public static final String REQUEST_PARAM_TO_PATH_NAME = "to";

    public static final String REQUEST_PART_FILES_NAME = "files";
}
