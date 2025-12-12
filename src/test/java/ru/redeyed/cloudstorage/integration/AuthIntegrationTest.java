package ru.redeyed.cloudstorage.integration;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.RedisSessionRepository;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.shaded.org.bouncycastle.util.encoders.Base64;
import ru.redeyed.cloudstorage.auth.dto.SignInRequestDto;
import ru.redeyed.cloudstorage.provider.InvalidSignInRequestDtoArgumentsProvider;
import ru.redeyed.cloudstorage.util.JsonUtil;
import ru.redeyed.cloudstorage.util.data.AuthTestData;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Authentication")
@RequiredArgsConstructor
class AuthIntegrationTest extends BaseIntegrationTest {

    private static final String SIGN_IN_URL = "/api/auth/sign-in";

    private static final String SESSION_KEYS_NAMESPACE = RedisSessionRepository.DEFAULT_KEY_NAMESPACE + ":sessions:";

    private static final String SESSION_SECURITY_CONTEXT_ATTRIBUTE =
            "sessionAttr:" + HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

    private static final String COOKIE_SESSION_NAME = "SESSION";

    private final MockMvc mockMvc;

    private final RedisIndexedSessionRepository redisSessionRepository;

    private final RedisTemplate<String, Object> redisTemplate;

    private RedisIndexedSessionRepository.RedisSession redisSession;

    @BeforeEach
    void createRedisSession() {
        var redisSession = redisSessionRepository.createSession();
        redisSessionRepository.save(redisSession);
        this.redisSession = redisSession;
    }

    @Nested
    @DisplayName("Sign In")
    class SignInIntegrationTest {

        @Test
        @DisplayName("User credentials are correct")
        void credentialsCorrect_shouldAuthenticateAndCreateNewUserSession() throws Exception {
            var signInRequestDto = AuthTestData.getSignInRequestDto();
            var jsonRequest = JsonUtil.toJson(signInRequestDto);

            var sessionId = getEncodedSessionId();
            var sessionCookie = new Cookie(COOKIE_SESSION_NAME, sessionId);

            var mvcResult = mockMvc.perform(post(SIGN_IN_URL)
                            .cookie(sessionCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpectAll(
                            status().isOk(),
                            cookie().value(COOKIE_SESSION_NAME, Matchers.not(sessionId))
                    )
                    .andReturn();

            var sessionKey = getSessionKey(mvcResult);
            var username = getAuthenticatedUsername(sessionKey);

            assertAll(
                    () -> assertTrue(redisTemplate.hasKey(sessionKey)),
                    () -> assertEquals(signInRequestDto.getUsername(), username)
            );
        }

        @Test
        @DisplayName("Incorrect password")
        void incorrectPassword_shouldReturnUnauthorized() throws Exception {
            var signInRequestDto = AuthTestData.getIncorrectSignInRequestDto(
                    SignInRequestDto.Fields.PASSWORD, "incorrectPassword"
            );

            var jsonRequest = JsonUtil.toJson(signInRequestDto);

            var sessionCookie = new Cookie(COOKIE_SESSION_NAME, getEncodedSessionId());

            mockMvc.perform(post(SIGN_IN_URL)
                            .cookie(sessionCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpectAll(
                            status().isUnauthorized(),
                            cookie().doesNotExist(COOKIE_SESSION_NAME)
                    );
        }

        @Test
        @DisplayName("User doesn't exist")
        void userDoesNotExist_shouldReturnUnauthorized() throws Exception {
            var signInRequestDto = AuthTestData.getIncorrectSignInRequestDto(
                    SignInRequestDto.Fields.USERNAME, "non_existent_user"
            );

            var jsonRequest = JsonUtil.toJson(signInRequestDto);

            var sessionCookie = new Cookie(COOKIE_SESSION_NAME, getEncodedSessionId());

            mockMvc.perform(post(SIGN_IN_URL)
                            .cookie(sessionCookie)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpectAll(
                            status().isUnauthorized(),
                            cookie().doesNotExist(COOKIE_SESSION_NAME)
                    );
        }

        @ParameterizedTest(name = "{1}")
        @DisplayName("Invalid request parameters")
        @ArgumentsSource(InvalidSignInRequestDtoArgumentsProvider.class)
        void invalidRequestParameter_shouldReturnBadRequest(SignInRequestDto signInRequestDto, String description) throws Exception {
            var jsonRequest = JsonUtil.toJson(signInRequestDto);

            mockMvc.perform(post(SIGN_IN_URL)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(jsonRequest))
                    .andExpectAll(
                            status().isBadRequest(),
                            cookie().doesNotExist(COOKIE_SESSION_NAME)
                    );
        }
    }

    private String getEncodedSessionId() {
        var sessionIdBytes = redisSession.getId().getBytes();
        var encodedSessionIdBytes = Base64.encode(sessionIdBytes);
        return new String(encodedSessionIdBytes);
    }

    private String getSessionKey(MvcResult mvcResult) {
        var response = mvcResult.getResponse();

        var sessionCookie = response.getCookie(COOKIE_SESSION_NAME);

        var encodedSessionId = Objects.requireNonNull(sessionCookie).getValue();
        var sessionId = new String(Base64.decode(encodedSessionId));

        return SESSION_KEYS_NAMESPACE + sessionId;
    }

    private String getAuthenticatedUsername(String sessionKey) {
        var securityContext = (SecurityContext) redisTemplate.opsForHash()
                .get(sessionKey, SESSION_SECURITY_CONTEXT_ATTRIBUTE);

        var authentication = securityContext.getAuthentication();

        var userDetails = (UserDetails) Objects.requireNonNull(authentication).getPrincipal();

        return Objects.requireNonNull(userDetails).getUsername();
    }
}
