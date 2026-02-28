package com.example.onlinevideo.DTO;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * AI 回复请求 DTO
 * 用于接收前端发送的 AI 回复生成请求
 */
@Data
public class AIReplyRequest {
    
    @NotNull(message = "视频 ID 不能为空")
    private Long videoId;
    
    @NotNull(message = "评论 ID 不能为空")
    private Long commentId;
    
    @NotBlank(message = "评论内容不能为空")
    private String commentContent;
    
    @NotBlank(message = "评论作者不能为空")
    private String commentAuthor;
    
    // 可选字段：父评论内容（如果是回复评论）
    private String parentCommentContent;
    
    // 可选字段：父评论作者（如果是回复评论）
    private String parentCommentAuthor;
}
