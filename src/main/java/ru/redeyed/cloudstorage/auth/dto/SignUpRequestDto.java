package ru.redeyed.cloudstorage.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldNameConstants;
import ru.redeyed.cloudstorage.user.validation.annotation.ValidPassword;
import ru.redeyed.cloudstorage.user.validation.annotation.ValidUsername;
import ru.redeyed.cloudstorage.user.validation.validator.PasswordValidator;
import ru.redeyed.cloudstorage.user.validation.validator.UsernameValidator;

@AllArgsConstructor
@Getter
@FieldNameConstants
public class SignUpRequestDto {

    @ValidUsername
    @Schema(
            minLength = UsernameValidator.MIN_LENGTH,
            maxLength = UsernameValidator.MAX_LENGTH,
            pattern = UsernameValidator.REGEX_PATTERN
    )
    private final String username;

    @Setter
    @ValidPassword
    @Schema(
            minLength = PasswordValidator.MIN_LENGTH,
            maxLength = PasswordValidator.MAX_LENGTH,
            pattern = PasswordValidator.REGEX_PATTERN
    )
    private String password;
}
