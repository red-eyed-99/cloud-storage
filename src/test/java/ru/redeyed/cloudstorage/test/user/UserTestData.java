package ru.redeyed.cloudstorage.test.user;

import lombok.experimental.UtilityClass;
import java.util.UUID;

@UtilityClass
public class UserTestData {

    public static final UUID ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

    public static final String USERNAME = "test_user";
    public static final String PASSWORD = "12345";
}
