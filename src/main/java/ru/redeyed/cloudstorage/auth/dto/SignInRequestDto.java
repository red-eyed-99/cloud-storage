package ru.redeyed.cloudstorage.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.redeyed.cloudstorage.validation.annotation.ValidPassword;
import ru.redeyed.cloudstorage.validation.annotation.ValidUsername;

@AllArgsConstructor
@Getter
public class SignInRequestDto {

    @ValidUsername
    private final String username;

    @Setter
    @ValidPassword
    private String password;
}
