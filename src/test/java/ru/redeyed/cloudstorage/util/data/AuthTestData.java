package ru.redeyed.cloudstorage.util.data;

import lombok.experimental.UtilityClass;
import ru.redeyed.cloudstorage.auth.dto.SignInRequestDto;
import ru.redeyed.cloudstorage.auth.dto.SignUpRequestDto;

@UtilityClass
public class AuthTestData {

    public static SignInRequestDto getSignInRequestDto() {
        return new SignInRequestDto(UserTestData.USERNAME, UserTestData.PASSWORD);
    }

    public static SignUpRequestDto getSignUpRequestDto() {
        return new SignUpRequestDto("new_" + UserTestData.USERNAME, UserTestData.PASSWORD);
    }
}
