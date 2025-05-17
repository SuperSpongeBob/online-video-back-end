package com.example.onlinevideo.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.Arrays;

@Configuration
public class CorsConfig{
    @Bean
    public CorsFilter corsFilter(){
        CorsConfiguration config = new CorsConfiguration();

        // 允许所有域名进行跨域调用
//        config.addAllowedOriginPattern("*");
        config.addAllowedOriginPattern("https://192.168.1.10");
        config.addAllowedOriginPattern("http://192.168.1.10");

        // 明确指定允许的来源
//        config.setAllowedOrigins(Arrays.asList("http://192.168.137.1:5173","http://192.168.1.1:5173","http://192.168.35.1:5173","http://localhost:5173"));

        // 允许任何请求头
        config.addAllowedHeader("*");

        // 允许任何方法（POST、GET等）
        config.addAllowedMethod("*");

        // 允许携带凭证
        config.setAllowCredentials(true);
        config.addExposedHeader("Authorization");

        // 设置预检请求的缓存时间（秒），在此时间内，相同的预检请求将不再发送
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
