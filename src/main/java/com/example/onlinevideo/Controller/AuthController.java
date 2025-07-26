package com.example.onlinevideo.Controller;

import com.example.onlinevideo.Annotation.RateLimit;
import com.example.onlinevideo.DTO.*;
import com.example.onlinevideo.Mapper.AuthMapper;
import com.example.onlinevideo.Security.JwtTokenProvider;
import com.example.onlinevideo.Service.ForgetPasswordService;
import com.example.onlinevideo.Service.TokenBlacklistService;
import com.example.onlinevideo.Vo.R;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
@Tag(name = "登录、注册、忘记密码")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthMapper authMapper;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ForgetPasswordService forgetPasswordService;

    @GetMapping("/test")
    public String test(){
        return "test ok";
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authMapper.login(loginRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null) {
            //  获取token剩下的到期时间
            Duration remainingExpiration = jwtTokenProvider.getRemainingExpiration(token);
            //  将退出登录的token添加到黑名单中，并设置过期时间与token同步
            tokenBlacklistService.addToBlacklist(token,remainingExpiration);
        }
        return ResponseEntity.ok("退出登录成功");
    }

    @PostMapping("/register/code")
    @RateLimit(maxRequests = 10)
    public R sendRegisterVerificationCode(@RequestBody RegisterRequest registerRequest) {
        return authMapper.sendRegisterVerificationCode(registerRequest.getUserPhone(), registerRequest.getUserEmail());
    }

    @PostMapping("/register/insert")
    @RateLimit(maxRequests = 4)
    public R insertUser(@RequestBody RegisterRequest registerRequest) {
        return authMapper.register(registerRequest);
    }

    @PostMapping("/forget-password/code")
    @RateLimit(maxRequests = 10)
    public R sendCode(@RequestBody ForgetPasswordRequest request) {
        return forgetPasswordService.sendVerificationCode(request.getUserPhone(), request.getUserEmail());
    }

    @PostMapping("forget-password/reset")
    @RateLimit(maxRequests = 4)
    public R resetPassword(@RequestBody ForgetPasswordRequest request) {
        return forgetPasswordService.resetPassword(request);
    }
}