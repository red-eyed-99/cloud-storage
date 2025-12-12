package ru.redeyed.cloudstorage.util;

import lombok.experimental.UtilityClass;
import tools.jackson.databind.ObjectMapper;

@UtilityClass
public class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String toJson(Object object) {
        return OBJECT_MAPPER.writeValueAsString(object);
    }
}
