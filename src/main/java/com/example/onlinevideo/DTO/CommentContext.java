package com.example.onlinevideo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 评论上下文 DTO
 * 用于封装评论相关信息，提供给 AI 生成回复时使用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentContext {
    
    private Long commentId;
    
    private String commentContent;
    
    private String commentAuthor;
    
    // 可选字段：父评论内容（如果是回复评论）
    private String parentCommentContent;
    
    // 可选字段：父评论作者（如果是回复评论）
    private String parentCommentAuthor;
}
