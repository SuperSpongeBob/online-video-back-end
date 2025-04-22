package com.example.onlinevideo.Exception;

import com.example.onlinevideo.DTO.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

@ControllerAdvice
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<?> handleExpiredJwtException(ExpiredJwtException exception){
        return new  ResponseEntity<>("Token expired",HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(RateLimitException.class)
    public ResponseEntity<?> handleRateLimitException(RateLimitException ex, WebRequest request) {
        ApiResponse<String> response = new ApiResponse<>();
        response.setCode(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setMessage(ex.getMessage());
        response.setData(null);
        //  返回 429 Too Many Requests 状态码
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
//        //  处理其他异常
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error!!!!!!!");
//    }
}
