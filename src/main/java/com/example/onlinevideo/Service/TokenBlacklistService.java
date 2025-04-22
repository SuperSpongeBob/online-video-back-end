package com.example.onlinevideo.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, Object> redisTemplate;

    // 将Token加入黑名单（并设置TTL与Token过期时间一致）
    public void addToBlacklist(String token, Duration ttl) {
        String key = "blacklist:" + token;
        redisTemplate.opsForValue().set(key, "invalid", ttl);
    }

    // 检查Token是否在黑名单中
    public boolean isTokenBlacklisted(String token) {
        String key = "blacklist:" + token;
        return redisTemplate.hasKey(key);
    }
}