package ru.redeyed.cloudstorage.test.auth.argumentsprovider;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.support.ParameterDeclarations;
import ru.redeyed.cloudstorage.auth.dto.SignInRequestDto;
import ru.redeyed.cloudstorage.test.auth.AuthTestData;
import ru.redeyed.cloudstorage.user.validation.validator.PasswordValidator;
import ru.redeyed.cloudstorage.user.validation.validator.UsernameValidator;
import ru.redeyed.cloudstorage.util.IncorrectTestDataUtil;
import java.util.stream.Stream;

public class InvalidSignInRequestDtoArgumentsProvider implements ArgumentsProvider {

    @Override
    public @NotNull Stream<? extends Arguments> provideArguments(@NotNull ParameterDeclarations parameters,
                                                                 @NotNull ExtensionContext context) {

        var invalidUsernameArguments = getInvalidUsernameArguments();
        var invalidPasswordArguments = getInvalidPasswordArguments();

        return Stream.concat(invalidUsernameArguments, invalidPasswordArguments);
    }

    private static Stream<Arguments> getInvalidUsernameArguments() {
        return Stream.of(
                IncorrectTestDataUtil.getNullArguments(
                        AuthTestData.getSignInRequestDto(), SignInRequestDto.Fields.USERNAME
                ),

                IncorrectTestDataUtil.getEmptyArguments(
                        AuthTestData.getSignInRequestDto(), SignInRequestDto.Fields.USERNAME
                ),

                IncorrectTestDataUtil.getMinLengthArguments(
                        AuthTestData.getSignInRequestDto(), SignInRequestDto.Fields.USERNAME,
                        "1234", UsernameValidator.MIN_LENGTH
                ),

                IncorrectTestDataUtil.getMaxLengthArguments(
                        AuthTestData.getSignInRequestDto(), SignInRequestDto.Fields.USERNAME,
                        "moreThanTwentyCharacters", UsernameValidator.MAX_LENGTH
                ),

                IncorrectTestDataUtil.getCyrillicArguments(
                        AuthTestData.getSignInRequestDto(), SignInRequestDto.Fields.USERNAME, "кириллица"
                ),

                IncorrectTestDataUtil.getStartsWithUnderscoreArguments(
                        AuthTestData.getSignInRequestDto(), SignInRequestDto.Fields.USERNAME, "_underscore"
                ),

                IncorrectTestDataUtil.getEndsWithUnderscoreArguments(
                        AuthTestData.getSignInRequestDto(), SignInRequestDto.Fields.USERNAME, "underscore_"
                )
        );
    }

    private static Stream<Arguments> getInvalidPasswordArguments() {
        return Stream.of(
                IncorrectTestDataUtil.getNullArguments(
                        AuthTestData.getSignInRequestDto(), SignInRequestDto.Fields.PASSWORD
                ),

                IncorrectTestDataUtil.getEmptyArguments(
                        AuthTestData.getSignInRequestDto(), SignInRequestDto.Fields.PASSWORD
                ),

                IncorrectTestDataUtil.getMinLengthArguments(
                        AuthTestData.getSignInRequestDto(), SignInRequestDto.Fields.PASSWORD,
                        "1234", PasswordValidator.MIN_LENGTH
                ),

                IncorrectTestDataUtil.getMaxLengthArguments(
                        AuthTestData.getSignInRequestDto(), SignInRequestDto.Fields.PASSWORD,
                        "moreThanTwentyCharacters", PasswordValidator.MAX_LENGTH
                ),

                IncorrectTestDataUtil.getCyrillicArguments(
                        AuthTestData.getSignInRequestDto(), SignInRequestDto.Fields.PASSWORD, "кириллица"
                )
        );
    }
}
