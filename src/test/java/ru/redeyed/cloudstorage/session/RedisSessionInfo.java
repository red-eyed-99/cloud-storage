package ru.redeyed.cloudstorage.session;

import jakarta.servlet.http.Cookie;

public record RedisSessionInfo(String id, String encodedId, String key, Cookie cookie) {
}
