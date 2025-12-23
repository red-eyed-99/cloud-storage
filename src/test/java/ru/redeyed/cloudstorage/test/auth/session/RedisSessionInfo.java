package ru.redeyed.cloudstorage.test.auth.session;

import jakarta.servlet.http.Cookie;

public record RedisSessionInfo(String id, String encodedId, String key, Cookie cookie) {
}
