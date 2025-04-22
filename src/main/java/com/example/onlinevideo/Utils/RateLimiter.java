package com.example.onlinevideo.Utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimiter {
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public boolean allowRequest(String key,int maxRequest,int timeWindow){
        //  生成Redis键
        String redisKey = key;

        //  获取当前请求次数
        Long currentRequests = redisTemplate.opsForValue().increment(redisKey,1);

        //  如果是第一次请求，设置过期事件
        if (currentRequests != null && currentRequests ==1){
            redisTemplate.expire(redisKey,timeWindow, TimeUnit.SECONDS);
        }

        //  检查是否超过最大请求次数
        return currentRequests != null && currentRequests <=maxRequest;
    }
}
