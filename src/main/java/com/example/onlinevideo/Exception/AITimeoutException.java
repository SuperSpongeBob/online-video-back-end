package com.example.onlinevideo.Exception;

/**
 * AI API 超时异常
 */
public class AITimeoutException extends AIServiceException {
    
    public AITimeoutException(String message) {
        super(message);
    }
    
    public AITimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
