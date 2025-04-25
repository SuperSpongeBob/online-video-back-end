package com.example.onlinevideo.Controller;

import com.example.onlinevideo.Annotation.CheckOwnership;
import com.example.onlinevideo.Annotation.RateLimit;
import com.example.onlinevideo.Entity.VideoAlbum;
import com.example.onlinevideo.Service.VideoAlbumService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "专辑")
@RestController
@RequestMapping("/api")
public class VideoAlbumController {
    @Autowired
    private VideoAlbumService videoAlbumService;

    //  根据用户id获取专辑信息
    @GetMapping("/videoAlbum/{userId}")
    public List<VideoAlbum> getVideoAlbumsByUserId(@PathVariable Integer userId) {
        VideoAlbum videoAlbum = new VideoAlbum();
        videoAlbum.setUserId(userId);
        List<VideoAlbum> VA=videoAlbumService.getVideoAlbums(videoAlbum);
        return VA;
    }

    /*
    * getVideoAlbumsByUserId的升级版
    * 可根据多种条件进行检索
    * */
    @PostMapping("/videoAlbums")
    public List<VideoAlbum> getVideoAlbums(@RequestBody VideoAlbum videoAlbum) {
        List<VideoAlbum> VA=videoAlbumService.getVideoAlbums(videoAlbum);
        return VA;
    }

    @PostMapping("/updateAlbum")
    @RateLimit(maxRequests = 10)
    @CheckOwnership(expression = "#videoAlbum.userId")
    public ResponseEntity<?> updateAlbum(@RequestBody VideoAlbum videoAlbum) {
        videoAlbum.setVideoPostPath(null);
        boolean result = videoAlbumService.updateVideoAlbum(videoAlbum);
        if (result) {
            return ResponseEntity.ok().build();
        }else {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/deleteAlbum")
    @RateLimit(maxRequests = 10)
    @CheckOwnership(expression = "#videoAlbum.userId")
    public ResponseEntity<?> deleteAlbum(@RequestBody VideoAlbum videoAlbum) {
        boolean delete = videoAlbumService.deleteAlbum(videoAlbum);
        return delete ? ResponseEntity.ok().build() : ResponseEntity.badRequest().build();
    }

}
