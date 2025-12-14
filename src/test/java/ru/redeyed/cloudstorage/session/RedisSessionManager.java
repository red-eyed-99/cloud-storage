package ru.redeyed.cloudstorage.session;

import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.RedisSessionRepository;
import org.springframework.stereotype.Component;
import ru.redeyed.cloudstorage.auth.UserDetailsImpl;
import ru.redeyed.cloudstorage.util.Base64Util;
import ru.redeyed.cloudstorage.util.data.UserTestData;
import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class RedisSessionManager {

    public static final String COOKIE_SESSION_NAME = "SESSION";

    public static final String SESSION_KEYS_NAMESPACE = RedisSessionRepository.DEFAULT_KEY_NAMESPACE + ":sessions:";

    public static final String SESSION_SECURITY_CONTEXT_ATTRIBUTE =
            "sessionAttr:" + HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

    private final RedisIndexedSessionRepository redisSessionRepository;

    public RedisSessionInfo getSessionInfo(RedisIndexedSessionRepository.RedisSession session) {
        var id = session.getId();
        var encodedId = Base64Util.encode(id);
        var key = SESSION_KEYS_NAMESPACE + id;
        var cookie = new Cookie(COOKIE_SESSION_NAME, encodedId);

        return new RedisSessionInfo(id, encodedId, key, cookie);
    }

    public RedisSessionInfo getSessionInfo(Cookie sessionCookie) {
        var encodedSessionId = Objects.requireNonNull(sessionCookie).getValue();
        var sessionId = Base64Util.decode(encodedSessionId);
        var sessionKey = SESSION_KEYS_NAMESPACE + sessionId;

        return new RedisSessionInfo(sessionId, encodedSessionId, sessionKey, sessionCookie);
    }

    public RedisIndexedSessionRepository.RedisSession createGuestSession() {
        var redisSession = redisSessionRepository.createSession();
        redisSessionRepository.save(redisSession);
        return redisSession;
    }

    public RedisIndexedSessionRepository.RedisSession createAuthenticatedSession() {
        var securityContext = SecurityContextHolder.createEmptyContext();

        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                new UserDetailsImpl(UserTestData.USERNAME, null), null, List.of()
        );

        securityContext.setAuthentication(authentication);

        var redisSession = redisSessionRepository.createSession();

        redisSession.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext
        );

        redisSessionRepository.save(redisSession);

        return redisSession;
    }
}
