package com.example.onlinevideo.Controller;

import com.example.onlinevideo.Service.VideoService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;

@RestController
@RequestMapping("/api")
public class VideoPlayController {
    @Autowired
    private VideoService videoService;

    @GetMapping("/video/play")
    public ResponseEntity<?> playVideo(
            @RequestParam String videoId,
            @RequestParam(required = false) String token,
            HttpServletResponse response) throws Exception {

        // 验证用户权限
        boolean isVip = videoService.verifyVideo(videoId,token);
        String videoPath = videoService.getVideoPathByVideoId(videoId);

        if (!isVip) {
            System.out.println("/video/preview/");
            // 非VIP用户返回预览URL
            String previewUrl = "/api/video/preview/" + URLEncoder.encode(videoId, "UTF-8");
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header("Location", previewUrl)
                    .build();
        }

        System.out.println("/internal/videos/");
        // VIP用户原始视频流
        response.setHeader("X-Accel-Redirect", "/internal/videos/" +
                URLEncoder.encode(videoPath, "UTF-8"));
        response.setHeader("Content-Type", "video/mp4");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/video/preview/{videoId}")
    public ResponseEntity<?> getPreview(
            @PathVariable String videoId,
            HttpServletResponse response) {

        System.out.println("getPreview");
        // 设置预览缓存头
        response.setHeader("X-Preview-Length", "120"); // 2分钟=120秒
        response.setHeader("X-Accel-Redirect", "/internal/previews/" + videoId);
        return ResponseEntity.ok().build();
    }
}
