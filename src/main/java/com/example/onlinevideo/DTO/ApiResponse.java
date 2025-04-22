package com.example.onlinevideo.DTO;

import lombok.Data;

/**
 * 数据传输对象（DTO）
 * 通用的响应类，用于封装API的返回数据
 * 统一API的响应格式
 * @param <T>
 */

@Data
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;
}
