package ru.redeyed.cloudstorage.integration;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import ru.redeyed.cloudstorage.argumentsprovider.InvalidSignInRequestDtoArgumentsProvider;
import ru.redeyed.cloudstorage.argumentsprovider.InvalidSignUpRequestDtoArgumentsProvider;
import ru.redeyed.cloudstorage.session.RedisSessionManager;
import ru.redeyed.cloudstorage.auth.dto.SignInRequestDto;
import ru.redeyed.cloudstorage.auth.dto.SignUpRequestDto;
import ru.redeyed.cloudstorage.user.UserService;
import ru.redeyed.cloudstorage.util.JsonUtil;
import ru.redeyed.cloudstorage.util.data.AuthTestData;
import ru.redeyed.cloudstorage.util.data.IncorrectTestDataUtil;
import ru.redeyed.cloudstorage.util.data.UserTestData;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Authentication")
@RequiredArgsConstructor
class AuthIntegrationTest extends BaseIntegrationTest {

    private static final String SIGN_IN_URL = "/api/auth/sign-in";
    private static final String SIGN_UP_URL = "/api/auth/sign-up";
    private static final String SIGN_OUT_URL = "/api/auth/sign-out";

    private final MockMvc mockMvc;

    private final UserService userService;

    private final RedisTemplate<String, Object> redisTemplate;

    private final RedisSessionManager redisSessionManager;

    @Nested
    @DisplayName("Sign In")
    class SignInIntegrationTest {

        @Test
        @DisplayName("User credentials are correct")
        void credentialsCorrect_shouldAuthenticateAndCreateNewUserSession() throws Exception {
            var signInRequestDto = AuthTestData.getSignInRequestDto();
            var jsonRequest = JsonUtil.toJson(signInRequestDto);
            var guestSession = redisSessionManager.createGuestSession();
            var guestSessionInfo = redisSessionManager.getSessionInfo(guestSession);

            var mvcResult = mockMvc.perform(post(SIGN_IN_URL)
                            .cookie(guestSessionInfo.cookie())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpectAll(
                            status().isOk(),
                            cookie().value(
                                    RedisSessionManager.COOKIE_SESSION_NAME,
                                    Matchers.not(guestSessionInfo.encodedId())
                            )
                    )
                    .andReturn();

            var newSessionCookie = getNewSessionCookie(mvcResult);
            var newSessionInfo = redisSessionManager.getSessionInfo(newSessionCookie);
            var username = getAuthenticatedUsername(newSessionInfo.key());

            assertAll(
                    () -> assertTrue(redisTemplate.hasKey(newSessionInfo.key())),
                    () -> assertFalse(redisTemplate.hasKey(guestSessionInfo.key())),
                    () -> assertEquals(signInRequestDto.getUsername(), username)
            );
        }

        @Test
        @DisplayName("Incorrect password")
        void incorrectPassword_shouldReturnUnauthorized() throws Exception {
            var signInRequestDto = IncorrectTestDataUtil.getIncorrectDto(
                    AuthTestData::getSignInRequestDto, SignInRequestDto.Fields.PASSWORD, "incorrectPassword"
            );
            var jsonRequest = JsonUtil.toJson(signInRequestDto);

            mockMvc.perform(post(SIGN_IN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpectAll(
                            status().isUnauthorized(),
                            cookie().doesNotExist(RedisSessionManager.COOKIE_SESSION_NAME)
                    );
        }

        @Test
        @DisplayName("User doesn't exist")
        void userDoesNotExist_shouldReturnUnauthorized() throws Exception {
            var signInRequestDto = IncorrectTestDataUtil.getIncorrectDto(
                    AuthTestData::getSignInRequestDto, SignInRequestDto.Fields.USERNAME, "non_existent_user"
            );
            var jsonRequest = JsonUtil.toJson(signInRequestDto);

            mockMvc.perform(post(SIGN_IN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpectAll(
                            status().isUnauthorized(),
                            cookie().doesNotExist(RedisSessionManager.COOKIE_SESSION_NAME)
                    );
        }

        @ParameterizedTest(name = "{1}")
        @DisplayName("Invalid request parameters")
        @ArgumentsSource(InvalidSignInRequestDtoArgumentsProvider.class)
        void invalidRequestParameter_shouldReturnBadRequest(SignInRequestDto signInRequestDto,
                                                            String description) throws Exception {

            var jsonRequest = JsonUtil.toJson(signInRequestDto);

            mockMvc.perform(post(SIGN_IN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpectAll(
                            status().isBadRequest(),
                            cookie().doesNotExist(RedisSessionManager.COOKIE_SESSION_NAME)
                    );
        }
    }

    @Nested
    @DisplayName("Sign Up")
    class SignUpIntegrationTest {

        @Test
        @DisplayName("User doesn't exist and credentials are correct")
        void userDoesNotExistAndCredentialsCorrect_shouldRegisterAndCreateNewUserSession() throws Exception {
            var signUpRequestDto = AuthTestData.getSignUpRequestDto();
            var jsonRequest = JsonUtil.toJson(signUpRequestDto);
            var guestSession = redisSessionManager.createGuestSession();
            var guestSessionInfo = redisSessionManager.getSessionInfo(guestSession);

            var mvcResult = mockMvc.perform(post(SIGN_UP_URL)
                            .cookie(guestSessionInfo.cookie())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpectAll(
                            status().isCreated(),
                            cookie().value(
                                    RedisSessionManager.COOKIE_SESSION_NAME,
                                    Matchers.not(guestSessionInfo.encodedId())
                            )
                    )
                    .andReturn();

            var newSessionCookie = getNewSessionCookie(mvcResult);
            var newSessionInfo = redisSessionManager.getSessionInfo(newSessionCookie);
            var username = getAuthenticatedUsername(newSessionInfo.key());
            var userDetails = userService.loadUserByUsername(signUpRequestDto.getUsername());

            assertAll(
                    () -> assertTrue(redisTemplate.hasKey(newSessionInfo.key())),
                    () -> assertFalse(redisTemplate.hasKey(guestSessionInfo.key())),
                    () -> assertEquals(signUpRequestDto.getUsername(), username),
                    () -> assertNotNull(userDetails)
            );
        }

        @Test
        @DisplayName("User already exists")
        void userAlreadyExists_shouldReturnConflict() throws Exception {
            var signUpRequestDto = IncorrectTestDataUtil.getIncorrectDto(
                    AuthTestData::getSignUpRequestDto, SignInRequestDto.Fields.USERNAME, UserTestData.USERNAME
            );
            var jsonRequest = JsonUtil.toJson(signUpRequestDto);

            mockMvc.perform(post(SIGN_UP_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpectAll(
                            status().isConflict(),
                            cookie().doesNotExist(RedisSessionManager.COOKIE_SESSION_NAME)
                    );
        }

        @ParameterizedTest(name = "{1}")
        @DisplayName("Invalid request parameters")
        @ArgumentsSource(InvalidSignUpRequestDtoArgumentsProvider.class)
        void invalidRequestParameter_shouldReturnBadRequest(SignUpRequestDto signUpRequestDto,
                                                            String description) throws Exception {

            var jsonRequest = JsonUtil.toJson(signUpRequestDto);

            mockMvc.perform(post(SIGN_UP_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpectAll(
                            status().isBadRequest(),
                            cookie().doesNotExist(RedisSessionManager.COOKIE_SESSION_NAME)
                    );
        }
    }

    @Nested
    @DisplayName("Sign Out")
    class SignOutIntegrationTest {

        @Test
        @DisplayName("User authorized")
        void userAuthorized_shouldSignOutUser() throws Exception {
            var authSession = redisSessionManager.createAuthenticatedSession();
            var authSessionInfo = redisSessionManager.getSessionInfo(authSession);

            mockMvc.perform(post(SIGN_OUT_URL)
                            .cookie(authSessionInfo.cookie()))
                    .andExpectAll(
                            status().isNoContent(),
                            cookie().value(
                                    RedisSessionManager.COOKIE_SESSION_NAME,
                                    Matchers.emptyString()
                            )
                    );

            assertFalse(redisTemplate.hasKey(authSessionInfo.key()));
        }

        @Test
        @DisplayName("User unauthorized")
        void userUnauthorized_shouldReturnUnauthorized() throws Exception {
            var guestSession = redisSessionManager.createGuestSession();
            var guestSessionInfo = redisSessionManager.getSessionInfo(guestSession);

            mockMvc.perform(post(SIGN_OUT_URL)
                            .cookie(guestSessionInfo.cookie()))
                    .andExpect(status().isUnauthorized());
        }
    }

    private Cookie getNewSessionCookie(MvcResult mvcResult) {
        return mvcResult.getResponse()
                .getCookie(RedisSessionManager.COOKIE_SESSION_NAME);
    }

    private String getAuthenticatedUsername(String sessionKey) {
        var securityContext = (SecurityContext) redisTemplate.opsForHash()
                .get(sessionKey, RedisSessionManager.SESSION_SECURITY_CONTEXT_ATTRIBUTE);

        var authentication = securityContext.getAuthentication();

        var userDetails = (UserDetails) Objects.requireNonNull(authentication).getPrincipal();

        return Objects.requireNonNull(userDetails).getUsername();
    }
}
