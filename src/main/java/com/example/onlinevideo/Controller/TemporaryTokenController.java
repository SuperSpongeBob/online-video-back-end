package com.example.onlinevideo.Controller;

import com.example.onlinevideo.Security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

@Tag(name = "生成临时token")
@RestController
public class TemporaryTokenController {

    private final JwtTokenProvider jwtTokenProvider;
    @Value("${jwt.secret}")
    private String SECRET_KEY ;
    private static final long EXPIRATION_TIME = 120*60 * 1000; // 临时 token 有效期 120 分钟

    public TemporaryTokenController(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping("/api/generate-temporary-token")
    public ResponseEntity<?> generateTemporaryToken(@RequestHeader(value = "Authorization",required = false) String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isEmpty()) {
            return ResponseEntity.ok().build();
        }
        // 提取当前 token
        String currentToken = authorizationHeader.replace("Bearer ", "");
        if (!jwtTokenProvider.validateToken(currentToken)) {
            return ResponseEntity.status(401).build();
        }

        //  从原始 token 中解析权限
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(currentToken).getBody();

        //  提取角色
        List<String> roles = (List<String>) claims.get("roles");

        //  生成带有权限的临时 token
        String temporaryToken= Jwts.builder()
                .setClaims(new HashMap<>())
                .setSubject(claims.getSubject())    //  保留原始用户名
                .claim("roles",roles)
                .claim("userId",claims.get("userId"))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis()+EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS256,SECRET_KEY)
                .compact();
        return ResponseEntity.ok(temporaryToken);
    }
}