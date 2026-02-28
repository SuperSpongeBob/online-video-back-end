package com.example.onlinevideo.Exception;

/**
 * AI API Key 无效异常
 */
public class AIInvalidKeyException extends AIServiceException {
    
    public AIInvalidKeyException(String message) {
        super(message);
    }
    
    public AIInvalidKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
