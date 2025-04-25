package com.example.onlinevideo.Controller;

import com.example.onlinevideo.Annotation.CheckOwnership;
import com.example.onlinevideo.Annotation.RateLimit;
import com.example.onlinevideo.DTO.UserDTO;
import com.example.onlinevideo.Entity.User;
import com.example.onlinevideo.Security.JwtTokenProvider;
import com.example.onlinevideo.Service.UserService;
import com.example.onlinevideo.Vo.R;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户")
@RestController
@RequestMapping("/api")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @PostMapping("/updateUserInfo")
    @RateLimit(maxRequests = 10)
    @CheckOwnership(expression = "#user.userId")
    public R updateUserInfo(@RequestBody User user) {
        return userService.updateUserInfo(user);
    }

    @PostMapping("/updateUserPassword")
    @RateLimit(maxRequests = 2)
    @CheckOwnership(expression = "#user.userId")
    public ResponseEntity<?> updateUserPassword(@RequestBody UserDTO user, HttpServletRequest request) {
        //  解析出token
        String token = jwtTokenProvider.resolveToken(request);
        if (token==null){
            return ResponseEntity.status(403).build();
        }

        //  从token中解析出userPhone
        String userPhone = jwtTokenProvider.getUsernameFromToken(token);

        //  更新密码
        boolean state = userService.updateUserPassword(user.getUserPassword(), user.getUserNewPassword(), userPhone);
        if (state) {
            return ResponseEntity.status(HttpStatus.OK).body(true);
        }
        return ResponseEntity.status(HttpServletResponse.SC_BAD_REQUEST).build();
    }

}
