package com.example.onlinevideo.Config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * DeepSeek AI API 配置类
 * 用于加载和管理 DeepSeek API 的相关配置参数
 */
@Configuration
@ConfigurationProperties(prefix = "deepseek")
@Data
public class DeepSeekConfig {
    
    /**
     * 是否启用 DeepSeek AI 功能
     */
    private boolean enabled = true;
    
    /**
     * DeepSeek API Key
     * 从环境变量 DEEPSEEK_API_KEY 加载
     */
    private String apiKey;
    
    /**
     * DeepSeek API 端点 URL
     * 默认: https://api.deepseek.com/v1/chat/completions
     */
    private String apiUrl = "https://api.deepseek.com/v1/chat/completions";
    
    /**
     * 使用的模型名称
     * 默认: deepseek-chat
     */
    private String model = "deepseek-chat";
    
    /**
     * Temperature 参数，控制生成文本的随机性
     * 范围: 0.0 - 1.0
     * 默认: 0.7 (平衡创意性和准确性)
     */
    private Double temperature = 0.7;
    
    /**
     * 生成回复的最大 token 数量
     * 默认: 300
     */
    private Integer maxTokens = 300;
    
    /**
     * API 请求超时时间（毫秒）
     * 默认: 30000 (30秒)
     */
    private Integer timeout = 30000;
    
    /**
     * 系统提示词模板
     * 定义 AI 的角色和行为准则
     */
    private String systemPrompt = "你是一个友好的视频评论回复助手。";
    
    /**
     * 检查 API Key 是否已配置
     * 
     * @return 如果 API Key 已配置且不为空，返回 true
     */
    public boolean isApiKeyConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
    
    /**
     * 检查 DeepSeek 服务是否可用
     * 
     * @return 如果服务已启用且 API Key 已配置，返回 true
     */
    public boolean isServiceAvailable() {
        return enabled && isApiKeyConfigured();
    }
    
    /**
     * 配置 RestTemplate Bean
     * 用于 HTTP 请求，设置超时时间
     * 
     * @param builder RestTemplateBuilder
     * @return 配置好的 RestTemplate
     */
    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(timeout != null ? timeout : 30000))
                .setReadTimeout(Duration.ofMillis(timeout != null ? timeout : 30000))
                .build();
    }
}
