package ru.redeyed.cloudstorage.util.data;

import lombok.experimental.UtilityClass;
import ru.redeyed.cloudstorage.auth.dto.SignInRequestDto;
import ru.redeyed.cloudstorage.util.IncorrectTestDataUtil;

@UtilityClass
public class AuthTestData {

    public static SignInRequestDto getSignInRequestDto() {
        return new SignInRequestDto(UserTestData.USERNAME, UserTestData.PASSWORD);
    }

    public static SignInRequestDto getIncorrectSignInRequestDto(String incorrectFieldName, Object incorrectFieldValue) {
        var signInRequestDto = getSignInRequestDto();
        IncorrectTestDataUtil.setIncorrectValue(signInRequestDto, incorrectFieldName, incorrectFieldValue);
        return signInRequestDto;
    }
}
