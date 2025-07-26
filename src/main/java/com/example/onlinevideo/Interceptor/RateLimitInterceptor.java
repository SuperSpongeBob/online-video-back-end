package com.example.onlinevideo.Interceptor;

import com.example.onlinevideo.Security.CustomUserDetails;
import com.example.onlinevideo.Utils.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.annotation.Around;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 作用于全局的拦截器
 */

@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    @Autowired
    private RateLimiter rateLimiter;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //  获取限流键
        String key = buildRateLimitKey(request);

        //  检查是否允许请求
        boolean allowRequest = rateLimiter.allowRequest(key,60,60);     //  一分钟之内最多请求 60 次
        if (!allowRequest) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.getWriter().write("Too many requests");
            return false;
        }

        return true;
    }

    /**
     * 构建限流键
     *
     * @param request   HTTP 请求对象
     * @return  限流键
     */

    private String buildRateLimitKey(HttpServletRequest request) {

        //  获取当前登录用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String path = request.getRequestURI();      //  请求的路径

        //  如果用户已登录，使用用户ID作为限流键
        if (authentication !=null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())){
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String userId = userDetails.getUserId().toString();
            return "Global_rate_limit:user:"+userId;

            //  如果用登录名作为限流键
//            String userPhone = (String) authentication.getName();     //  userPhone:这个项目用的是电话号码作为security的userName
//            return "rate_limit:user:"+userPhone;
        }

        //  如果用户未登录，使用 IP + User-Agent 作为限流键
        String userAgent = request.getHeader("User-Agent");
        return "Global_rate_limit:ip:"+request.getRemoteAddr()+":ua:"+userAgent;

    }
}
