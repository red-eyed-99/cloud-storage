package ru.redeyed.cloudstorage.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.ClassPathResource;
import ru.redeyed.cloudstorage.common.util.StringUtil;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import java.nio.charset.Charset;
import java.util.HashMap;

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

    public static <T> T fromJson(String json, TypeReference<T> typeReference) {
        return OBJECT_MAPPER.readValue(json, typeReference);
    }

    public String removeUnnecessaryCharacters(String json) {
        var regexesReplacements = new HashMap<String, String>();

        regexesReplacements.put("[\n\r]", "");
        regexesReplacements.put(": ", ":");
        regexesReplacements.put("\\s{2,}", "");

        return StringUtil.removeUnnecessaryCharacters(json, regexesReplacements);
    }
}
