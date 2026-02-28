package com.example.onlinevideo.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 视频上下文 DTO
 * 用于封装视频相关信息，提供给 AI 生成回复时使用
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VideoContext {
    
    private Long videoId;
    
    private String videoName;
    
    private String videoTitle;
    
    private String videoType;
    
    private String uploaderName;
}
