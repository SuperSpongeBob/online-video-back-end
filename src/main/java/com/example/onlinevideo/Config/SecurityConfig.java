package com.example.onlinevideo.Config;

import com.example.onlinevideo.Security.*;
import com.example.onlinevideo.Service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Date;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)  //  启用方法级别的安全性
public class SecurityConfig {
    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
                                                           TokenBlacklistService tokenBlacklistService,
                                                           CustomUserDetailsService customUserDetailsService) {
        return new JwtAuthenticationFilter(jwtTokenProvider, tokenBlacklistService, customUserDetailsService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        //  禁止表单提交
        http.formLogin().disable();
        http.httpBasic().disable();
        //  授权配置
        http.authorizeRequests(authorize->authorize
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/update**","/api/delete**","/api/insert**","/api/add**","/api/upload")
                .hasAnyRole("ADMIN","VIP","USER")
                .requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/swagger-resources/**",
                        "/v3/api-docs/**",
                        "/webjars/**",
                        "/api-docs/**",
                        "/api/videos",
                        "/api/auth/**",
                        "/api/searchVideo",
                        "/api/videoComment",
                        "/api/getDanmaku",
                        "/api/IndexVideos",
                        "/api/recommendVideos",
                        "/api/increaseViewCount",
                        "/api/existsVideo",
                        "/api/verify",
                        "/api/generate-temporary-token",
                        "/api/static/video").permitAll()
                .anyRequest().authenticated());

        //  禁用角色前缀
        http.securityContext().requireExplicitSave(false);

        //  自定义无权限返回信息
        http.exceptionHandling(handing->handing
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json; charset=utf-8");
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.getWriter().write("{\"code\":403,\"message\":\"权限不足禁止访问\"}");
                    logger.warn("权限拒绝：{}", accessDeniedException.getMessage());
                }))
                //  将默认的session设置为无状态，弃用session
                .sessionManagement(session->session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .addFilterBefore(jwtAuthenticationFilter(null,null,null), UsernamePasswordAuthenticationFilter.class);

        http.csrf(AbstractHttpConfigurer::disable);
        http.cors();
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
