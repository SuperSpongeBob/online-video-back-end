package com.example.onlinevideo.Exception;

/**
 * AI 服务基础异常类
 */
public class AIServiceException extends RuntimeException {
    
    public AIServiceException(String message) {
        super(message);
    }
    
    public AIServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
