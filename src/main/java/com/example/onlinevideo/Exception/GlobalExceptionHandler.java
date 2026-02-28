package com.example.onlinevideo.Exception;

import com.example.onlinevideo.DTO.ApiResponse;
import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
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
    
    /**
     * 处理 AI 超时异常
     * Requirements: 7.2
     */
    @ExceptionHandler(AITimeoutException.class)
    public ResponseEntity<?> handleAITimeoutException(AITimeoutException ex, WebRequest request) {
        log.error("AI 服务超时异常: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "请求超时，请稍后重试");
        errorResponse.put("errorCode", "AI_TIMEOUT");
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(errorResponse);
    }
    
    /**
     * 处理 API Key 无效异常
     * Requirements: 7.3
     */
    @ExceptionHandler(AIInvalidKeyException.class)
    public ResponseEntity<?> handleAIInvalidKeyException(AIInvalidKeyException ex, WebRequest request) {
        log.error("AI API Key 无效异常: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "服务配置错误");
        errorResponse.put("errorCode", "AI_INVALID_KEY");
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(errorResponse);
    }
    
    /**
     * 处理网络连接异常
     * Requirements: 7.4
     */
    @ExceptionHandler(AINetworkException.class)
    public ResponseEntity<?> handleAINetworkException(AINetworkException ex, WebRequest request) {
        log.error("AI 服务网络连接异常: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "网络连接失败");
        errorResponse.put("errorCode", "AI_NETWORK_ERROR");
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(errorResponse);
    }
    
    /**
     * 处理空响应异常
     * Requirements: 7.5
     */
    @ExceptionHandler(AIEmptyResponseException.class)
    public ResponseEntity<?> handleAIEmptyResponseException(AIEmptyResponseException ex, WebRequest request) {
        log.error("AI 服务空响应异常: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", "未能生成回复，请重试");
        errorResponse.put("errorCode", "AI_EMPTY_RESPONSE");
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }
    
    /**
     * 处理通用 AI 服务异常
     * Requirements: 7.1, 7.6, 7.7
     */
    @ExceptionHandler(AIServiceException.class)
    public ResponseEntity<?> handleAIServiceException(AIServiceException ex, WebRequest request) {
        log.error("AI 服务异常: {}", ex.getMessage(), ex);
        
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("success", false);
        errorResponse.put("error", ex.getMessage());
        errorResponse.put("errorCode", "AI_SERVICE_ERROR");
        errorResponse.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<?> handleGlobalException(Exception ex, WebRequest request) {
//        //  处理其他异常
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error!!!!!!!");
//    }
}
