package ru.redeyed.cloudstorage.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.ClassPathResource;
import tools.jackson.databind.ObjectMapper;
import java.nio.charset.Charset;

@UtilityClass
public class JsonUtil {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static String toJson(Object object) {
        return OBJECT_MAPPER.writeValueAsString(object);
    }

    @SneakyThrows
    public static String getJsonFrom(String path) {
        return new ClassPathResource(path)
                .getContentAsString(Charset.defaultCharset());
    }
}
