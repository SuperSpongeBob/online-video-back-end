package com.example.onlinevideo.Controller;

import com.example.onlinevideo.Annotation.CheckOwnership;
import com.example.onlinevideo.Annotation.RateLimit;
import com.example.onlinevideo.DTO.VideoDTO;
import com.example.onlinevideo.Entity.Video;
import com.example.onlinevideo.Enum.VideoApprovalStatus;
import com.example.onlinevideo.Mapper.VideoMapper;
import com.example.onlinevideo.Security.JwtTokenProvider;
import com.example.onlinevideo.Service.VideoRecommendService;
import com.example.onlinevideo.Service.VideoService;
import com.example.onlinevideo.Vo.R;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "视频")
@Slf4j
@RestController
@RequestMapping("/api")
public class VideoController {
    @Autowired
    private VideoService videoService;
    @Autowired
    private VideoRecommendService videoRecommendService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    @Autowired
    private VideoMapper videoMapper;

    //  观看视频
    @GetMapping("/static/video")
    public ResponseEntity<?> getVideoUrl(@RequestParam("videoId") String videoId, HttpServletRequest request, HttpServletResponse response,
                                         @RequestParam(value = "token", required = false) String token) throws Exception {
        // 首先获取视频类型代码
        Integer videoTypeCode = videoMapper.verifyVideo(videoId);
        if (videoTypeCode == null) {
            return ResponseEntity.status(HttpServletResponse.SC_NOT_FOUND).body("视频不存在");
        }
        
        String videoPath = videoService.getVideoPathByVideoId(videoId);
        if (videoPath == null || videoPath.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        
        String finalVideoPath;
        String redirectPath = "/videos/";
        boolean isAdmin = false;
        
        // 检查是否为无版权视频（code = 3）
        if (videoTypeCode == 3) {
            // 无版权视频：管理员观看完整视频，非管理员观看30秒预览
            String authToken = jwtTokenProvider.resolveToken(request);
            if (authToken == null || authToken.isEmpty()) {
                authToken = token; // 使用参数中的token作为备选
            }
            
            if (authToken != null && !authToken.isEmpty()) {
                try {
                    Claims claims = Jwts.parser()
                            .setSigningKey(SECRET_KEY)
                            .parseClaimsJws(authToken.replace("Bearer ", ""))
                            .getBody();
                    List<String> roles = (List<String>) claims.get("roles");
                    
                    // 检查是否是管理员
                    if (roles != null && roles.contains("ROLE_ADMIN")) {
                        isAdmin = true;
                        // 管理员可以观看完整视频
                        finalVideoPath = videoPath;
                    } else {
                        // 非管理员用户，返回30秒预览版本
                        finalVideoPath = videoPath.startsWith("preview_") ? videoPath : "preview_" + videoPath;
                    }
                } catch (Exception e) {
                    // Token解析失败，返回预览版本（允许未登录用户观看30秒）
                    finalVideoPath = videoPath.startsWith("preview_") ? videoPath : "preview_" + videoPath;
                }
            } else {
                // 未登录用户，返回30秒预览版本
                finalVideoPath = videoPath.startsWith("preview_") ? videoPath : "preview_" + videoPath;
            }
        } else {
            //  验证视频类型（对于非无版权视频）
            boolean isVip = videoService.verifyVideo(videoId, token);  //  false:可以观看  true：不可以观看

            if (isVip) {
                // 确保只添加一次"preview_"前缀
                finalVideoPath = videoPath.startsWith("preview_") ? videoPath : "preview_" + videoPath;
//            redirectPath = "/videos-preview/";
            } else {
                finalVideoPath = videoPath;
//            redirectPath = "/videos/";
            }
        }

        // 对视频路径进行 URL 编码
        String encodedVideoPath = URLEncoder.encode(finalVideoPath, StandardCharsets.UTF_8);
        response.setHeader("X-Accel-Redirect", redirectPath + encodedVideoPath);
        response.setHeader("Content-Type", "video/mp4");

        System.out.println(redirectPath + encodedVideoPath);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/deleteVideo")
    @RateLimit(maxRequests = 10)
    @CheckOwnership(expression = "@videoAlbumService.getOwnerId(#video.videoAlbumId)")
    public ResponseEntity<?> deleteVideo(@RequestBody Video video) {
        try {
            boolean aBoolean = videoService.deleteVideo(video.getVideoId());
            if (aBoolean) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("删除视频异常",e.getMessage());
            e.printStackTrace();
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/updateVideo")
    @RateLimit(maxRequests = 10)
    @CheckOwnership(expression = "@videoAlbumService.getOwnerId(#video.videoAlbumId)")
    public ResponseEntity<?> updateVideo(@RequestBody Video video) {
        //  非管理员禁止设置视频的审核状态，设置为 null 将无法进行更改
        video.setVideoApprovalStatus(null);
        //  禁止更新视频、图片，视频、图片更新已经在videoUploadController实现
        video.setVideoPath(null);
        video.setThumbnailPath(null);
        boolean aBoolean = videoService.updateVideo(video);
        if (aBoolean) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/searchVideo")
    public ResponseEntity<?> searchVideo(@RequestBody Video video) {
        try {
            //  非管理员等身份只能获取审核通过的视频
            video.setVideoApprovalStatus(VideoApprovalStatus.REVIEW_PASSED);
            List<Video> videos = videoService.SearchVideo(video);
            return new ResponseEntity<>(videos, HttpStatus.OK);
        } catch (Exception e) {
            log.error("搜索视频异常",e.getMessage());
            e.printStackTrace();
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    //搜索、首页获取视频信息，分页获取
    @PostMapping("/IndexVideos")
    public ResponseEntity<?> getIndexVideos(@RequestBody Video video) {
        //  非管理员等身份只能获取审核通过的视频
        video.setVideoApprovalStatus(VideoApprovalStatus.REVIEW_PASSED);
        List<Video> videos = videoService.getIndexVideo(video);
        if (!videos.isEmpty()) {
            return ResponseEntity.ok(videos);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    //  根据视频播放量和最新视频进行推荐
    @GetMapping("/recommendVideos")
    public ResponseEntity<List<VideoDTO>> recommendVideo(@RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size,
                                                      HttpServletRequest request) {
        // 获取token
        String token = new JwtTokenProvider().resolveToken(request);
        if (token == null) {
            //  如果token为空则用user-agent代替
            token = request.getHeader("User-Agent");
        }

        // 如果是第一页，重置推荐池
        if (page == 1) {
            videoRecommendService.resetRecommendPool(token);
        }

        List<VideoDTO> videos = videoRecommendService.getRecommendVideos(page - 1, size, token);

        if (!videos.isEmpty()) {
            return ResponseEntity.ok(videos);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    //搜索、首页获取视频信息，分页获取
    @PostMapping("/videos")
    public ResponseEntity<?> getVideos(@RequestBody Video video) {
        List<Video> videos = videoService.getIndexVideo(video);
        if (!videos.isEmpty()) {
            return ResponseEntity.ok(videos);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/videosByUserId")
    @CheckOwnership(expression = "#userId")
    public ResponseEntity<?> getVideosByUserId(@RequestParam(value = "userId") Integer userId) {
        List<Video> videos = videoService.videosByUserId(userId);
        if (!videos.isEmpty()) {
            return ResponseEntity.ok(videos);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @GetMapping("/videosInSameAlbum")
    public ResponseEntity<?> getVideoByAlbumId(@RequestParam(value = "videoId") Integer videoId) {
        List<VideoDTO> videos = videoService.videosInSameAlbum(videoId);
        if (!videos.isEmpty()) {
            return ResponseEntity.ok(videos);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    //根据id验证是否能观看视频     仅用于返回信息给用户，具体能否观看不受影响
    @GetMapping("/verify")
    public ResponseEntity<?> verifyVip(@RequestParam(value = "videoId") String videoId, HttpServletRequest request) {
        // 首先获取视频类型代码（tinyint: 0-免费, 1-收费, 2-VIP, 3-无版权, 4-独播）
        Integer videoTypeCode = videoMapper.verifyVideo(videoId);
        if (videoTypeCode == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("canWatch", false);
            errorResponse.put("reason", "视频不存在");
            errorResponse.put("message", "该视频不存在或已被删除");
            return ResponseEntity.status(HttpServletResponse.SC_NOT_FOUND).body(errorResponse);
        }
        
        // 获取视频类型和源视频链接信息
        Map<String, Object> videoInfo = videoService.getVideoTypeAndSourceUrl(Integer.parseInt(videoId));
        
        // 检查是否为无版权视频（code = 3）
        if (videoTypeCode == 3) {
            // 无版权视频：管理员观看完整视频，非管理员观看30秒预览
            String token = jwtTokenProvider.resolveToken(request);
            boolean isAdmin = false;
            
            if (token != null) {
                try {
                    Claims claims = Jwts.parser()
                            .setSigningKey(SECRET_KEY)
                            .parseClaimsJws(token.replace("Bearer ", ""))
                            .getBody();
                    List<String> roles = (List<String>) claims.get("roles");
                    
                    // 检查是否是管理员
                    if (roles != null && roles.contains("ROLE_ADMIN")) {
                        isAdmin = true;
                    }
                } catch (Exception e) {
                    // Token解析失败，视为非管理员
                }
            }
            
            // 构建响应
            Map<String, Object> response = new HashMap<>();
            response.put("canWatch", true); // 所有用户都可以观看（30秒预览或完整视频）
            response.put("videoType", videoInfo != null ? videoInfo.get("videoType") : null);
            response.put("sourceVideoUrl", videoInfo != null ? videoInfo.get("sourceVideoUrl") : null);
            
            // 如果不是管理员，标记30秒限制
            if (!isAdmin) {
                response.put("reason", "NO_COPYRIGHT");
                response.put("message", "该视频为无版权视频，仅可观看30秒预览");
                response.put("maxWatchTime", 30); // 30秒限制
            }
            
            return ResponseEntity.ok().body(response);
        }
        
        // 对于其他类型的视频，使用原有逻辑
        String token = jwtTokenProvider.resolveToken(request);
        if (token != null) {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody();
            List<String> roles = (List<String>) claims.get("roles");

            if (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_VIP")) {
                // 管理员或VIP用户，获取视频信息
                Map<String, Object> response = new HashMap<>();
                response.put("canWatch", true);
                response.put("videoType", videoInfo != null ? videoInfo.get("videoType") : null);
                response.put("sourceVideoUrl", videoInfo != null ? videoInfo.get("sourceVideoUrl") : null);
                return ResponseEntity.ok().body(response);
            }
        }
        //  判断视频是否为VIP
        boolean vip = videoService.verifyVideo(videoId, token);  //  true: 还需要进行用户权限校验     false: 直接放行，说明视频为免费视频或者改视频为用户发布的视频
        if (vip) {
            Map<String, Object> response = new HashMap<>();
            response.put("canWatch", false);
            response.put("reason", "VIP_REQUIRED");
            response.put("message", "该视频为VIP专享，请升级会员后观看");
            response.put("videoType", videoInfo != null ? videoInfo.get("videoType") : null);
            response.put("sourceVideoUrl", videoInfo != null ? videoInfo.get("sourceVideoUrl") : null);
            return ResponseEntity.ok().body(response);
        }
        
        // 可以观看的视频
        Map<String, Object> response = new HashMap<>();
        response.put("canWatch", true);
        response.put("videoType", videoInfo != null ? videoInfo.get("videoType") : null);
        response.put("sourceVideoUrl", videoInfo != null ? videoInfo.get("sourceVideoUrl") : null);
        return ResponseEntity.ok().body(response);
    }

    //  验证视频是否存在
    @PostMapping("/existsVideo")
    public ResponseEntity<?> existsVideo(@RequestBody Video video) {
        boolean exists = videoService.existsVideo(video);
        return ResponseEntity.ok().body(exists);

    }

    //  添加视频播放量
    @PostMapping("/increaseViewCount")
    public void increaseViewCount(@RequestBody Map<String, Integer> request) {
        Integer videoId = request.get("videoId");
        videoService.increaseViewCount(videoId);
    }

    //  用户重申视频
    @PostMapping("/reiterateVideo")
    @CheckOwnership(expression = "@videoAlbumService.getOwnerId(#video.videoAlbumId)")
    public ResponseEntity<?> reiterateVideo(@RequestBody Video video) {
        boolean result = videoMapper.reiterateVideo(video.getVideoId());
        if (result) {
            return ResponseEntity.ok().body(true);
        }
        return ResponseEntity.badRequest().body(false);
    }
}
