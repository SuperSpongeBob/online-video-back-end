package com.example.onlinevideo.Controller;

import com.example.onlinevideo.Annotation.RateLimit;
import com.example.onlinevideo.DTO.AIReplyRequest;
import com.example.onlinevideo.DTO.AIReplyResponse;
import com.example.onlinevideo.DTO.CommentContext;
import com.example.onlinevideo.DTO.VideoContext;
import com.example.onlinevideo.Entity.User;
import com.example.onlinevideo.Entity.Video;
import com.example.onlinevideo.Enum.VideoType;
import com.example.onlinevideo.Mapper.UserMapper;
import com.example.onlinevideo.Mapper.VideoMapper;
import com.example.onlinevideo.Security.CurrentUserService;
import com.example.onlinevideo.Service.DeepSeekService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

/**
 * AI 评论回复控制器
 * 提供 AI 生成评论回复的 API 端点
 */
@Tag(name = "AI 评论回复")
@RestController
@RequestMapping("/api/ai")
public class AIReplyController {
    
    private static final Logger log = LoggerFactory.getLogger(AIReplyController.class);
    
    @Autowired
    private DeepSeekService deepSeekService;
    
    @Autowired
    private VideoMapper videoMapper;
    
    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private CurrentUserService currentUserService;
    
    /**
     * 生成 AI 评论回复
     * 
     * @param request AI 回复请求
     * @param bindingResult 验证结果
     * @return AI 回复响应
     */
    @Operation(summary = "生成 AI 评论回复", description = "基于视频和评论上下文生成智能回复建议")
    @PostMapping("/generate-reply")
    @RateLimit(maxRequests = 10)
    public ResponseEntity<AIReplyResponse> generateReply(
            @Valid @RequestBody AIReplyRequest request,
            BindingResult bindingResult) {
        
        log.info("收到 AI 回复生成请求 - 视频ID: {}, 评论ID: {}", 
                 request.getVideoId(), request.getCommentId());
        
        try {
            // 1. 验证用户认证
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
                log.warn("未认证用户尝试访问 AI 回复功能");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(AIReplyResponse.error("请先登录"));
            }
            
            Integer currentUserId = currentUserService.getCurrentUserId();
            log.debug("当前用户ID: {}", currentUserId);
            
            // 2. 验证请求参数
            if (bindingResult.hasErrors()) {
                String errorMessage = bindingResult.getFieldError() != null 
                        ? bindingResult.getFieldError().getDefaultMessage() 
                        : "请求参数无效";
                log.warn("请求参数验证失败: {}", errorMessage);
                return ResponseEntity.badRequest()
                        .body(AIReplyResponse.error(errorMessage));
            }
            
            // ===== 关键优化：在这里完成所有数据库操作 =====
            // 3. 从数据库获取视频信息并立即提取所需数据
            VideoContext videoContext;
            boolean hasPermission;
            
            {
                // 使用代码块限制变量作用域，确保数据库对象不会泄漏到 API 调用阶段
                Optional<Video> videoOptional = videoMapper.findVideoByVideoId(request.getVideoId().intValue());
                if (videoOptional.isEmpty()) {
                    log.warn("视频不存在 - 视频ID: {}", request.getVideoId());
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body(AIReplyResponse.error("视频不存在"));
                }
                
                Video video = videoOptional.get();
                log.debug("找到视频 - 名称: {}, 类型: {}", video.getVideoName(), video.getVideoType());
                
                // 4. 验证用户权限
                /*hasPermission = checkVideoPermission(video, currentUserId);
                if (!hasPermission) {
                    log.warn("用户无权限访问视频 - 用户ID: {}, 视频ID: {}", 
                             currentUserId, request.getVideoId());
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body(AIReplyResponse.error("您没有权限访问该视频"));
                }*/
                
                // 5. 立即构建视频上下文（提取所有需要的数据）
                videoContext = buildVideoContext(video);
                
                // 此时 video 对象会在代码块结束后被垃圾回收
                // 数据库连接可以被释放
            }
            
            // 6. 构建评论上下文（使用请求中的数据，不涉及数据库）
            CommentContext commentContext = new CommentContext(
                    request.getCommentId(),
                    request.getCommentContent(),
                    request.getCommentAuthor(),
                    request.getParentCommentContent(),
                    request.getParentCommentAuthor()
            );
            
            // ===== 此时所有数据库操作已完成，连接已释放 =====
            // 7. 调用 DeepSeekService 生成回复（这个操作可能需要 30 秒）
            log.debug("所有数据库操作已完成，开始调用 DeepSeekService");
            String replyText = deepSeekService.generateReply(videoContext, commentContext);
            
            log.info("AI 回复生成成功 - 视频ID: {}, 评论ID: {}, 回复长度: {}", 
                     request.getVideoId(), request.getCommentId(), replyText.length());
            
            // 8. 返回成功响应
            return ResponseEntity.ok(AIReplyResponse.success(replyText));
            
        } catch (IllegalStateException e) {
            // 用户认证相关错误
            log.error("用户认证错误", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AIReplyResponse.error("用户认证失败"));
            
        } catch (RuntimeException e) {
            // DeepSeek 服务相关错误
            log.error("AI 回复生成失败", e);
            
            String errorMessage = e.getMessage();
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            
            // 如果错误消息为 null，使用默认消息
            if (errorMessage == null) {
                errorMessage = "AI 服务暂时不可用";
                status = HttpStatus.SERVICE_UNAVAILABLE;
            } else {
                // 根据错误消息返回适当的状态码和消息
                if (errorMessage.contains("请求超时")) {
                    errorMessage = "请求超时，请稍后重试";
                    status = HttpStatus.REQUEST_TIMEOUT;
                } else if (errorMessage.contains("API Key")) {
                    errorMessage = "服务配置错误";
                    status = HttpStatus.SERVICE_UNAVAILABLE;
                } else if (errorMessage.contains("网络连接失败")) {
                    errorMessage = "网络连接失败";
                    status = HttpStatus.SERVICE_UNAVAILABLE;
                } else if (errorMessage.contains("未能生成回复")) {
                    errorMessage = "未能生成回复，请重试";
                    status = HttpStatus.INTERNAL_SERVER_ERROR;
                } else if (errorMessage.contains("服务未配置") || errorMessage.contains("服务不可用")) {
                    errorMessage = "AI 服务暂时不可用";
                    status = HttpStatus.SERVICE_UNAVAILABLE;
                }
            }
            
            return ResponseEntity.status(status)
                    .body(AIReplyResponse.error(errorMessage));
            
        } catch (Exception e) {
            // 其他未预期的错误
            log.error("处理 AI 回复请求时发生未预期错误", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(AIReplyResponse.error("服务器内部错误"));
        }
    }
    
    /**
     * 检查用户是否有权限访问视频
     * 
     * @param video 视频对象
     * @param userId 用户ID
     * @return 是否有权限
     */
    private boolean checkVideoPermission(Video video, Integer userId) {
        // 管理员可以访问所有视频
        if (currentUserService.isAdmin()) {
            log.debug("管理员用户，允许访问");
            return true;
        }
        
        VideoType videoType = video.getVideoType();
        
        // 免费视频所有人都可以访问
        if (videoType == VideoType.FREE) {
            log.debug("免费视频，允许访问");
            return true;
        }
        
        // 检查是否是视频上传者
        // 优先从 video.videoAlbum.userId 获取，避免额外查询
        Integer uploaderId = null;
        if (video.getVideoAlbum() != null && video.getVideoAlbum().getUserId() != null) {
            uploaderId = video.getVideoAlbum().getUserId();
        } else {
            // 如果 videoAlbum 没有加载，才进行查询
            uploaderId = userMapper.getUserIdByVideoId(video.getVideoId());
        }
        
        if (uploaderId != null && userId.equals(uploaderId)) {
            log.debug("视频上传者，允许访问");
            return true;
        }
        
        // VIP视频需要VIP权限
        if (videoType == VideoType.VIP) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            boolean isVip = authentication.getAuthorities().stream()
                    .anyMatch(authority -> authority.getAuthority().equals("ROLE_VIP"));
            
            if (isVip) {
                log.debug("VIP用户，允许访问VIP视频");
                return true;
            } else {
                log.debug("非VIP用户，拒绝访问VIP视频");
                return false;
            }
        }
        
        // 无版权视频允许访问（虽然只能看30秒，但可以评论）
        if (videoType == VideoType.NO_COPYRIGHT) {
            log.debug("无版权视频，允许访问");
            return true;
        }
        
        // 其他情况默认允许（如付费视频等）
        log.debug("其他视频类型，默认允许访问");
        return true;
    }
    
    /**
     * 构建视频上下文
     * 
     * @param video 视频对象
     * @return 视频上下文
     */
    private VideoContext buildVideoContext(Video video) {
        // 从 video 对象中提取所有需要的数据
        // 注意：不要在这里进行额外的数据库查询
        
        String uploaderName = null;
        
        // 尝试从 video 的关联对象中获取上传者信息
        if (video.getVideoAlbum() != null && video.getVideoAlbum().getUserId() != null) {
            Integer uploaderId = video.getVideoAlbum().getUserId();
            // 简化处理：直接使用 userId 作为名称
            // 如果需要真实用户名，应该在调用此方法前就查询好
            uploaderName = "用户" + uploaderId;
        }
        
        String videoTypeName = video.getVideoType() != null 
                ? video.getVideoType().name() 
                : "UNKNOWN";
        
        // 创建独立的 DTO 对象，包含所有需要的数据
        // 这样即使原始 video 对象被回收，videoContext 仍然可用
        return new VideoContext(
                video.getVideoId().longValue(),
                video.getVideoName() != null ? video.getVideoName() : "",
                video.getVideoTitle() != null ? video.getVideoTitle() : "",
                videoTypeName,
                uploaderName
        );
    }
}
