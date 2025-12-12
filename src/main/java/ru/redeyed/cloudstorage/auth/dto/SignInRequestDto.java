package ru.redeyed.cloudstorage.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import ru.redeyed.cloudstorage.common.validation.annotation.ValidPassword;
import ru.redeyed.cloudstorage.common.validation.annotation.ValidUsername;

@AllArgsConstructor
@Getter
@FieldNameConstants
public class SignInRequestDto {

    @ValidUsername
    private final String username;

    @Setter
    @ValidPassword
    private String password;
}
