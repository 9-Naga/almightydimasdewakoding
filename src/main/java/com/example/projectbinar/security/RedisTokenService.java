package com.example.projectbinar.security;

import java.util.concurrent.TimeUnit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisTokenService {

  private static final String TOKEN_PREFIX = "jwt_token:";
  private static final String BLACKLIST_PREFIX = "jwt_blacklist:";

  private final RedisTemplate<String, Object> redisTemplate;

  public RedisTokenService(RedisTemplate<String, Object> redisTemplate) {
    this.redisTemplate = redisTemplate;
  }

  /** Store a valid token in Redis */
  public void storeToken(String username, String token, long expirationMs) {
    String key = TOKEN_PREFIX + username;
    redisTemplate.opsForValue().set(key, token, expirationMs, TimeUnit.MILLISECONDS);
  }

  /** Get stored token for a user */
  public String getStoredToken(String username) {
    String key = TOKEN_PREFIX + username;
    Object token = redisTemplate.opsForValue().get(key);
    return token != null ? token.toString() : null;
  }

  /** Remove token from Redis (logout) */
  public void removeToken(String username) {
    String key = TOKEN_PREFIX + username;
    redisTemplate.delete(key);
  }

  /** Add token to blacklist (invalidate) */
  public void blacklistToken(String token, long expirationMs) {
    String key = BLACKLIST_PREFIX + token;
    redisTemplate.opsForValue().set(key, "blacklisted", expirationMs, TimeUnit.MILLISECONDS);
  }

  /** Check if token is blacklisted */
  public boolean isTokenBlacklisted(String token) {
    String key = BLACKLIST_PREFIX + token;
    return Boolean.TRUE.equals(redisTemplate.hasKey(key));
  }

  /** Validate token is not blacklisted and matches stored token */
  public boolean isTokenValid(String username, String token) {
    if (isTokenBlacklisted(token)) {
      return false;
    }

    String storedToken = getStoredToken(username);
    return storedToken != null && storedToken.equals(token);
  }
}
