package com.example.onlinevideo.Controller;

import com.example.onlinevideo.Annotation.CheckOwnership;
import com.example.onlinevideo.Annotation.RateLimit;
import com.example.onlinevideo.Entity.VideoReportRecord;
import com.example.onlinevideo.Enum.ReportStatus;
import com.example.onlinevideo.Service.VideoReportRecordService;
import com.example.onlinevideo.Security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Tag(name = "视频举报")
@Slf4j
@RestController
@RequestMapping("/api")
public class VideoReportRecordController {
    
    @Autowired
    private VideoReportRecordService videoReportRecordService;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    
    /**
     * 提交举报
     */
    @PostMapping("/submitReport")
    @RateLimit(maxRequests = 10)
    public ResponseEntity<?> submitReport(@RequestBody VideoReportRecord record, HttpServletRequest request) {
        try {
            // 从token中获取用户ID
            String token = jwtTokenProvider.resolveToken(request);
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("请先登录");
            }
            
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody();
            
            Integer userId = claims.get("userId", Integer.class);
            record.setReporterId(userId);
            
            boolean result = videoReportRecordService.submitReport(record);
            if (result) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "举报提交成功，我们会尽快处理");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body("举报提交失败");
            }
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("提交举报异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("服务器错误");
        }
    }
    
    /**
     * 检查用户是否已举报过该视频
     */
    @GetMapping("/checkReport")
    public ResponseEntity<?> checkReport(@RequestParam Integer videoId, HttpServletRequest request) {
        try {
            String token = jwtTokenProvider.resolveToken(request);
            if (token == null) {
                return ResponseEntity.ok(false);
            }
            
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody();
            
            Integer userId = claims.get("userId", Integer.class);
            boolean hasReported = videoReportRecordService.hasUserReported(userId, videoId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("hasReported", hasReported);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("检查举报状态异常", e);
            return ResponseEntity.ok(false);
        }
    }
    
    /**
     * 获取我的举报记录
     */
    @GetMapping("/myReports")
    @CheckOwnership(expression = "#userId")
    public ResponseEntity<?> getMyReports(@RequestParam Integer userId) {
        try {
            List<VideoReportRecord> records = videoReportRecordService.findReportRecordsByReporterId(userId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            log.error("获取举报记录异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("服务器错误");
        }
    }
    
    /**
     * 管理员：获取所有举报记录（分页）
     */
    @PostMapping("/admin/reports")
    public ResponseEntity<?> getAllReports(@RequestBody VideoReportRecord record) {
        try {
            List<VideoReportRecord> records = videoReportRecordService.findReportRecords(record);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            log.error("获取举报记录异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("服务器错误");
        }
    }
    
    /**
     * 管理员：处理举报
     */
    @PostMapping("/admin/handleReport")
    public ResponseEntity<?> handleReport(@RequestBody Map<String, Object> request) {
        try {
            Integer reportId = (Integer) request.get("reportId");
            Integer handlerId = (Integer) request.get("handlerId");
            String statusStr = (String) request.get("status");
            String handleRemark = (String) request.get("handleRemark");
            
            ReportStatus status = ReportStatus.fromDescription(statusStr);
            
            boolean result = videoReportRecordService.handleReport(reportId, handlerId, status, handleRemark);
            if (result) {
                return ResponseEntity.ok().body("处理成功");
            } else {
                return ResponseEntity.badRequest().body("处理失败");
            }
        } catch (Exception e) {
            log.error("处理举报异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("服务器错误");
        }
    }
    
    /**
     * 管理员：根据视频ID获取举报记录
     */
    @GetMapping("/admin/reportsByVideo")
    public ResponseEntity<?> getReportsByVideo(@RequestParam Integer videoId) {
        try {
            List<VideoReportRecord> records = videoReportRecordService.findReportRecordsByVideoId(videoId);
            return ResponseEntity.ok(records);
        } catch (Exception e) {
            log.error("获取举报记录异常", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("服务器错误");
        }
    }
}

