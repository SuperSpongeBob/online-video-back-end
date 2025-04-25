package com.example.onlinevideo.Controller;

import com.example.onlinevideo.Annotation.CheckOwnership;
import com.example.onlinevideo.Annotation.RateLimit;
import com.example.onlinevideo.Entity.VideoComment;
import com.example.onlinevideo.Service.VideoCommentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "评论")
@Controller
@RequestMapping("/api")
public class VideoCommentController {
    @Autowired
    private VideoCommentService videoCommentService;

    //根据视频Id获取评论
    @PostMapping("/videoComment")
    public ResponseEntity<?> getVideoComments(@RequestBody VideoComment videoComment) {
        List<VideoComment> list = videoCommentService.getVideoCommentsByVideoId(videoComment);
        if (!list.isEmpty()) {
            return ResponseEntity.ok(list);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    //  添加评论
    @PostMapping("/addComment")
    @RateLimit(maxRequests = 30)
    @CheckOwnership(expression = "#videoComment.userId")
    public ResponseEntity<?> addVideoComment(@RequestBody VideoComment videoComment) {
        Boolean state = videoCommentService.addVideoComment(videoComment);
        if (state) {
            return ResponseEntity.ok().body(true);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    //  删除评论
    @PostMapping("/deleteComment")
    public ResponseEntity<?> deleteVideoComment(@RequestBody VideoComment videoComment) {
        boolean result = videoCommentService.deleteComment(videoComment);
        if (result) {
            return ResponseEntity.ok().body(true);
        }
        return ResponseEntity.badRequest().build();
    }

}
