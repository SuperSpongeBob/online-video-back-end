package com.example.onlinevideo.Aspect;

import com.example.onlinevideo.Annotation.RateLimit;
import com.example.onlinevideo.Exception.RateLimitException;
import com.example.onlinevideo.Security.CustomUserDetails;
import com.example.onlinevideo.Utils.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AOP切面类
 * 仅作用于带有 @RateLimit 注解的方法
 */

@Aspect
@Component
public class RateLimitAspect {
    @Autowired
    private RateLimiter rateLimiter;

    @Around("@annotation(rateLimit)")   //  拦截带有 @RateLimit 注解的方法
    public Object around(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        //  获取请求对象
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        //  获取限流键（IP地址、User-Agent / 用户ID）
        String key = buildRateLimitKey(request,rateLimit);

        //  检查是否允许请求
        boolean allowRequest = rateLimiter.allowRequest(key,rateLimit.maxRequests(),rateLimit.timeWindow());
        if (!allowRequest) {
            throw new RateLimitException("请求过于频繁，请稍后再试");
        }

        return joinPoint.proceed();
    }

    /**
     * 构建限流键
     *
     * @param request   HTTP 请求对象
     * @param rateLimit 限流注解
     * @return  限流键
     */
    private String buildRateLimitKey(HttpServletRequest request,RateLimit rateLimit) {
        //  如果注解中指定了key，直接使用
        if (!rateLimit.key().isEmpty()){
            return rateLimit.key();
        }

        //  获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String path = request.getRequestURI();      //  请求的路径

        //  如果用户已登录，使用用户ID作为限流键
        if (authentication !=null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())){
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String userId = userDetails.getUserId().toString();
//            System.out.println("rate_limit:user:" + userId + ":path:" + path);
            return "rate_limit:user:"+userId+":path:"+path;

            //  如果用登录名作为限流键
//            String userPhone = (String) authentication.getName();     //  userPhone:这个项目用的是电话号码作为security的userName
//            return "rate_limit:user:"+userPhone;
        }

        //  如果用户未登录，使用 IP + User-Agent +path 作为限流键
        String userAgent = request.getHeader("User-Agent");
//        System.out.println("rate_limit:ip:" + request.getRemoteAddr() + ":ua:" + userAgent + ":path:" + path);
        return "rate_limit:ip:"+request.getRemoteAddr()+":ua:"+userAgent+":path:"+path;

    }
}
