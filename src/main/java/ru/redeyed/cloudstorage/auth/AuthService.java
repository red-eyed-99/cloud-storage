package ru.redeyed.cloudstorage.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.redeyed.cloudstorage.auth.dto.SignInRequestDto;
import ru.redeyed.cloudstorage.auth.dto.SignUpRequestDto;
import ru.redeyed.cloudstorage.user.UserService;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;

    private final AuthUserMapper authUserMapper;

    private final AuthenticationManager authenticationManager;

    private final PasswordEncoder passwordEncoder;

    public Authentication authenticate(SignInRequestDto signInRequestDto) {
        var authToken = getUnauthenticatedToken(signInRequestDto);

        signInRequestDto.setPassword(null);

        var authentication = authenticationManager.authenticate(authToken);

        eraseCredentials(authToken, authentication);

        return authentication;
    }

    public Authentication authenticate(SignUpRequestDto signUpRequestDto) {
        var authToken = getAuthenticatedToken(signUpRequestDto);

        encodePassword(signUpRequestDto);

        var createUserDto = authUserMapper.toCreateUserDto(signUpRequestDto);

        userService.create(createUserDto);

        return authToken;
    }

    private UsernamePasswordAuthenticationToken getAuthenticatedToken(SignUpRequestDto signUpRequestDto) {
        var username = signUpRequestDto.getUsername();
        var userDetails = new UserDetailsImpl(username, null);

        return UsernamePasswordAuthenticationToken
                .authenticated(username, null, List.of())
                .toBuilder()
                .principal(userDetails)
                .build();
    }

    private UsernamePasswordAuthenticationToken getUnauthenticatedToken(SignInRequestDto signInRequestDto) {
        return UsernamePasswordAuthenticationToken.unauthenticated(
                signInRequestDto.getUsername(), signInRequestDto.getPassword()
        );
    }

    private void encodePassword(SignUpRequestDto signUpRequestDto) {
        var encodedPassword = passwordEncoder.encode(signUpRequestDto.getPassword());
        signUpRequestDto.setPassword(encodedPassword);
    }

    private void eraseCredentials(UsernamePasswordAuthenticationToken token, Authentication authentication) {
        var userDetails = (UserDetailsImpl) authentication.getPrincipal();

        Objects.requireNonNull(userDetails).erasePassword();

        token.eraseCredentials();
    }
}
