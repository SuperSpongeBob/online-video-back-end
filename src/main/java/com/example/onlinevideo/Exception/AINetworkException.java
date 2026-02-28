package com.example.onlinevideo.Exception;

/**
 * AI API 网络连接异常
 */
public class AINetworkException extends AIServiceException {
    
    public AINetworkException(String message) {
        super(message);
    }
    
    public AINetworkException(String message, Throwable cause) {
        super(message, cause);
    }
}
