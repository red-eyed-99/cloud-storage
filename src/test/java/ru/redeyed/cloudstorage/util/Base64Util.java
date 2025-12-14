package ru.redeyed.cloudstorage.util;

import lombok.experimental.UtilityClass;
import org.testcontainers.shaded.org.bouncycastle.util.encoders.Base64;

@UtilityClass
public class Base64Util {

    public static String encode(String value) {
        var bytes = value.getBytes();
        var encodedBytes = Base64.encode(bytes);
        return new String(encodedBytes);
    }

    public static String decode(String value) {
        var decodedBytes = Base64.decode(value);
        return new String(decodedBytes);
    }
}
