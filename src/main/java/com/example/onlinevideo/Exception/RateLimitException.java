package com.example.onlinevideo.Exception;

public class RateLimitException extends RuntimeException{
    public RateLimitException(String message){
        super(message);
    }
}
