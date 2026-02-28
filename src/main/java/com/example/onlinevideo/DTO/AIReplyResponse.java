package com.example.onlinevideo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * AI 回复响应 DTO
 * 用于返回 AI 生成的回复内容给前端
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AIReplyResponse {
    
    private boolean success;
    
    private String replyText;
    
    private String error;
    
    /**
     * 成功响应构造器
     */
    public AIReplyResponse(boolean success, String replyText) {
        this.success = success;
        this.replyText = replyText;
    }
    
    /**
     * 错误响应构造器
     */
    public static AIReplyResponse error(String errorMessage) {
        AIReplyResponse response = new AIReplyResponse();
        response.setSuccess(false);
        response.setError(errorMessage);
        return response;
    }
    
    /**
     * 成功响应构造器
     */
    public static AIReplyResponse success(String replyText) {
        AIReplyResponse response = new AIReplyResponse();
        response.setSuccess(true);
        response.setReplyText(replyText);
        return response;
    }
}
