package com.example.onlinevideo.Exception;

/**
 * AI API 空响应异常
 */
public class AIEmptyResponseException extends AIServiceException {
    
    public AIEmptyResponseException(String message) {
        super(message);
    }
    
    public AIEmptyResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
