package com.example.onlinevideo.Service;

import com.example.onlinevideo.Config.DeepSeekConfig;
import com.example.onlinevideo.DTO.CommentContext;
import com.example.onlinevideo.DTO.VideoContext;
import com.example.onlinevideo.Exception.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;

import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;

/**
 * DeepSeek AI 服务类
 * 负责调用 DeepSeek API 生成评论回复
 */
@Service
public class DeepSeekService {
    
    private static final Logger log = LoggerFactory.getLogger(DeepSeekService.class);
    
    @Autowired
    private DeepSeekConfig deepSeekConfig;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    // 敏感信息正则模式
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "\\b\\d{3}[-.]?\\d{3,4}[-.]?\\d{4}\\b"
    );
    private static final Pattern URL_PATTERN = Pattern.compile(
        "https?://[\\w\\-]+(\\.[\\w\\-]+)+[/#?]?.*"
    );
    
    /**
     * 生成 AI 回复
     * 
     * @param videoContext 视频上下文信息
     * @param commentContext 评论上下文信息
     * @return 生成的回复文本
     * @throws AIServiceException 如果 API 调用失败
     * @throws AITimeoutException 如果 API 请求超时
     * @throws AIInvalidKeyException 如果 API Key 无效
     * @throws AINetworkException 如果网络连接失败
     * @throws AIEmptyResponseException 如果 API 返回空响应
     */
    public String generateReply(VideoContext videoContext, CommentContext commentContext) {
        log.info("开始生成 AI 回复 - 视频ID: {}, 评论ID: {}", 
                 videoContext.getVideoId(), commentContext.getCommentId());
        
        // 验证服务是否可用
        if (!deepSeekConfig.isServiceAvailable()) {
            log.error("DeepSeek 服务不可用 - enabled: {}, apiKeyConfigured: {}", 
                     deepSeekConfig.isEnabled(), deepSeekConfig.isApiKeyConfigured());
            throw new AIServiceException("DeepSeek 服务未配置或未启用");
        }
        
        // 验证和过滤输入
        validateAndSanitizeInput(videoContext, commentContext);
        
        // 构建提示词
        String systemPrompt = buildSystemPrompt();
        String userMessage = buildUserMessage(videoContext, commentContext);
        
        // 调用 DeepSeek API
        String reply = callDeepSeekAPI(systemPrompt, userMessage);
        
        log.info("AI 回复生成成功 - 视频ID: {}, 评论ID: {}", 
                 videoContext.getVideoId(), commentContext.getCommentId());
        
        return reply;
    }
    
    /**
     * 构建系统提示词
     * 
     * @return 系统提示词
     */
    private String buildSystemPrompt() {
        String systemPrompt = deepSeekConfig.getSystemPrompt();
        
        // 如果配置的提示词为空或只是默认值，使用完整的提示词
        if (systemPrompt == null || systemPrompt.trim().isEmpty() 
            || systemPrompt.equals("你是一个友好的视频评论回复助手。")) {
            systemPrompt = """
                你是一个友好的视频评论回复助手。你的任务是帮助用户生成对视频评论的回复。
                
                请遵循以下原则：
                1. 回复应该友好、礼貌且相关
                2. 回复应该基于视频内容和原评论的内容
                3. 回复应该简洁明了，建议在50-200字之间
                4. 回复应该避免敏感话题和不当内容
                5. 如果评论是提问，尝试提供有帮助的回答
                6. 如果评论是赞美，表达感谢
                7. 如果评论是批评，以建设性的方式回应
                
                请直接生成回复内容，不需要额外的解释或前缀。
                """;
        }
        
        return systemPrompt;
    }
    
    /**
     * 构建用户消息
     * 
     * @param videoContext 视频上下文
     * @param commentContext 评论上下文
     * @return 用户消息
     */
    private String buildUserMessage(VideoContext videoContext, CommentContext commentContext) {
        StringBuilder message = new StringBuilder();
        
        message.append("视频信息：\n");
        message.append("- 标题：").append(sanitizeText(videoContext.getVideoName())).append("\n");
        
        if (videoContext.getVideoTitle() != null && !videoContext.getVideoTitle().trim().isEmpty()) {
            message.append("- 描述：").append(sanitizeText(videoContext.getVideoTitle())).append("\n");
        }
        
        if (videoContext.getVideoType() != null && !videoContext.getVideoType().trim().isEmpty()) {
            message.append("- 类型：").append(sanitizeText(videoContext.getVideoType())).append("\n");
        }
        
        if (videoContext.getUploaderName() != null && !videoContext.getUploaderName().trim().isEmpty()) {
            message.append("- 上传者：").append(sanitizeText(videoContext.getUploaderName())).append("\n");
        }
        
        message.append("\n评论信息：\n");
        message.append("- 评论者：").append(sanitizeText(commentContext.getCommentAuthor())).append("\n");
        message.append("- 评论内容：").append(sanitizeText(commentContext.getCommentContent())).append("\n");
        
        // 如果是回复评论，添加父评论信息
        if (commentContext.getParentCommentContent() != null 
            && !commentContext.getParentCommentContent().trim().isEmpty()) {
            message.append("\n原评论信息：\n");
            message.append("- 原评论者：").append(sanitizeText(commentContext.getParentCommentAuthor())).append("\n");
            message.append("- 原评论内容：").append(sanitizeText(commentContext.getParentCommentContent())).append("\n");
        }
        
        message.append("\n请为这条评论生成一个合适的回复。");
        
        return message.toString();
    }
    
    /**
     * 调用 DeepSeek API
     * 
     * @param systemPrompt 系统提示词
     * @param userMessage 用户消息
     * @return 生成的回复文本
     * @throws AITimeoutException 如果 API 请求超时
     * @throws AIInvalidKeyException 如果 API Key 无效
     * @throws AINetworkException 如果网络连接失败
     * @throws AIEmptyResponseException 如果 API 返回空响应
     * @throws AIServiceException 如果其他 API 调用失败
     */
    private String callDeepSeekAPI(String systemPrompt, String userMessage) {
        try {
            // 构建请求体
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", deepSeekConfig.getModel());
            requestBody.put("temperature", deepSeekConfig.getTemperature());
            requestBody.put("max_tokens", deepSeekConfig.getMaxTokens());
            requestBody.put("stream", false);
            
            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            
            Map<String, String> systemMessage = new HashMap<>();
            systemMessage.put("role", "system");
            systemMessage.put("content", systemPrompt);
            messages.add(systemMessage);
            
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);
            messages.add(userMsg);
            
            requestBody.put("messages", messages);
            
            // 构建请求头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + deepSeekConfig.getApiKey());
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            
            log.debug("调用 DeepSeek API - URL: {}, Model: {}", 
                     deepSeekConfig.getApiUrl(), deepSeekConfig.getModel());
            
            // 发送请求
            ResponseEntity<String> response = restTemplate.exchange(
                deepSeekConfig.getApiUrl(),
                HttpMethod.POST,
                entity,
                String.class
            );
            
            // 解析响应
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                return parseAPIResponse(response.getBody());
            } else {
                log.error("DeepSeek API 返回非 200 状态码: {}", response.getStatusCode());
                throw new AIServiceException("DeepSeek API 调用失败");
            }
            
        } catch (HttpClientErrorException e) {
            log.error("DeepSeek API 客户端错误 - 状态码: {}, 响应: {}", 
                     e.getStatusCode(), e.getResponseBodyAsString());
            
            if (e.getStatusCode() == HttpStatus.UNAUTHORIZED || e.getStatusCode() == HttpStatus.FORBIDDEN) {
                throw new AIInvalidKeyException("API Key 无效或过期", e);
            } else if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                throw new AIServiceException("API 请求频率超限，请稍后重试", e);
            } else {
                throw new AIServiceException("API 请求失败: " + e.getMessage(), e);
            }
            
        } catch (HttpServerErrorException e) {
            log.error("DeepSeek API 服务器错误 - 状态码: {}, 响应: {}", 
                     e.getStatusCode(), e.getResponseBodyAsString());
            throw new AIServiceException("DeepSeek 服务暂时不可用，请稍后重试", e);
            
        } catch (ResourceAccessException e) {
            log.error("DeepSeek API 网络连接失败", e);
            
            if (e.getCause() instanceof java.net.SocketTimeoutException) {
                throw new AITimeoutException("请求超时，请稍后重试", e);
            } else {
                throw new AINetworkException("网络连接失败", e);
            }
            
        } catch (AIServiceException e) {
            // 重新抛出已知的 AI 异常
            throw e;
            
        } catch (Exception e) {
            log.error("DeepSeek API 调用异常", e);
            throw new AIServiceException("AI 回复生成失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解析 API 响应
     * 
     * @param responseBody 响应体 JSON 字符串
     * @return 提取的回复文本
     * @throws AIEmptyResponseException 如果解析失败或响应为空
     */
    private String parseAPIResponse(String responseBody) {
        try {
            JsonNode root = objectMapper.readTree(responseBody);
            
            // 提取 choices[0].message.content
            JsonNode choices = root.get("choices");
            if (choices == null || !choices.isArray() || choices.size() == 0) {
                log.error("API 响应中没有 choices 字段或为空");
                throw new AIEmptyResponseException("未能生成回复，请重试");
            }
            
            JsonNode firstChoice = choices.get(0);
            JsonNode message = firstChoice.get("message");
            if (message == null) {
                log.error("API 响应中没有 message 字段");
                throw new AIEmptyResponseException("未能生成回复，请重试");
            }
            
            JsonNode content = message.get("content");
            if (content == null || content.asText().trim().isEmpty()) {
                log.error("API 响应中 content 为空");
                throw new AIEmptyResponseException("未能生成回复，请重试");
            }
            
            String replyText = content.asText().trim();
            log.debug("成功解析 API 响应，回复长度: {}", replyText.length());
            
            return replyText;
            
        } catch (AIEmptyResponseException e) {
            // 重新抛出已知的空响应异常
            throw e;
            
        } catch (Exception e) {
            log.error("解析 API 响应失败", e);
            throw new AIEmptyResponseException("解析 AI 回复失败", e);
        }
    }
    
    /**
     * 验证和过滤输入内容
     * 
     * @param videoContext 视频上下文
     * @param commentContext 评论上下文
     * @throws AIServiceException 如果输入无效
     */
    private void validateAndSanitizeInput(VideoContext videoContext, CommentContext commentContext) {
        // 验证必填字段
        if (videoContext == null || commentContext == null) {
            throw new AIServiceException("视频或评论上下文不能为空");
        }
        
        if (commentContext.getCommentContent() == null 
            || commentContext.getCommentContent().trim().isEmpty()) {
            throw new AIServiceException("评论内容不能为空");
        }
        
        // 验证内容长度
        if (commentContext.getCommentContent().length() > 5000) {
            throw new AIServiceException("评论内容过长");
        }
        
        // 检查敏感信息（记录警告但不阻止）
        checkSensitiveData(commentContext.getCommentContent(), "评论内容");
        
        if (videoContext.getVideoTitle() != null) {
            checkSensitiveData(videoContext.getVideoTitle(), "视频描述");
        }
    }
    
    /**
     * 检查文本中是否包含敏感信息
     * 
     * @param text 要检查的文本
     * @param fieldName 字段名称（用于日志）
     */
    private void checkSensitiveData(String text, String fieldName) {
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        
        if (EMAIL_PATTERN.matcher(text).find()) {
            log.warn("{}中检测到邮箱地址", fieldName);
        }
        
        if (PHONE_PATTERN.matcher(text).find()) {
            log.warn("{}中检测到电话号码", fieldName);
        }
        
        if (URL_PATTERN.matcher(text).find()) {
            log.debug("{}中检测到 URL", fieldName);
        }
    }
    
    /**
     * 清理文本内容，移除潜在的危险字符
     * 
     * @param text 原始文本
     * @return 清理后的文本
     */
    private String sanitizeText(String text) {
        if (text == null) {
            return "";
        }
        
        // 移除控制字符，但保留换行和制表符
        return text.replaceAll("[\\p{Cntrl}&&[^\n\t\r]]", "").trim();
    }
}
