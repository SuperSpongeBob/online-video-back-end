package com.example.onlinevideo.Controller;

import com.example.onlinevideo.Annotation.RateLimit;
import com.example.onlinevideo.DTO.RegisterRequest;
import com.example.onlinevideo.DTO.UserDTO;
import com.example.onlinevideo.Entity.User;
import com.example.onlinevideo.Entity.UserRole;
import com.example.onlinevideo.Entity.Video;
import com.example.onlinevideo.Entity.VideoAlbum;
import com.example.onlinevideo.Mapper.VideoAlbumMapper;
import com.example.onlinevideo.Service.UserRoleService;
import com.example.onlinevideo.Service.UserService;
import com.example.onlinevideo.Service.VideoAlbumService;
import com.example.onlinevideo.Service.VideoService;
import com.example.onlinevideo.Vo.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "管理员接口")
@RequestMapping("/api/admin")
public class AdminController {
    @Autowired
    private UserService userService;
    @Autowired
    private VideoService videoService;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private VideoAlbumService videoAlbumService;
    @Autowired
    private VideoAlbumMapper videoAlbumMapper;

    @PostMapping("/users")      //  获取用户信息
    @Operation(summary = "获取用户信息")
    public ResponseEntity<?> getUsers(@RequestBody User user,
                                      @RequestParam(required = false) Long startTime,
                                      @RequestParam(required = false) Long endTime) {
        List<UserDTO> users = userService.getUsers(user, startTime, endTime);      //  获取user表中的所有用户信息
        if (!users.isEmpty()) {
            return ResponseEntity.ok(users);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/insertUser")
    @Operation(summary = "插入新用户")
    public ResponseEntity<?> insertUser(@RequestBody RegisterRequest request) {
        User user = User.builder()
                .userPhone(request.getUserPhone())
                .userPassword(request.getUserPassword())
                .userEmail(request.getUserEmail())
                .userGender(request.getUserGender())
                .userName(request.getUserName())
                .userAddTime(System.currentTimeMillis())
                .build();
        //  注册用户，向数据库插入用户信息
        boolean result = userService.insertUserWithRole(user,request.getRoleId());
        if (result) {
            return ResponseEntity.ok().body(true);
        }
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/updateUser")                 //  更新用户信息
    @Operation(summary = "更新用户信息")
    public R updateUser(@RequestBody UserDTO user) {
        userRoleService.updateUserRole(UserRole.builder().userId(user.getUserId()).roleId(user.getRoleId()).build());
        return userService.updateUser(user);   //  修改用户其他信息
    }

    @DeleteMapping("/deleteUser/{userId}")      //  根据用户id删除用户
    @Operation(summary = "根据用户Id删除用户")
    @RateLimit(maxRequests = 10)
    public ResponseEntity<?> deleteUser(@PathVariable Integer userId) {
        List<Integer> videoAlbumIds = videoAlbumMapper.findAlbumIdsByUserId(userId);
        for (Integer videoAlbumId : videoAlbumIds) {
            videoAlbumService.deleteAlbum(VideoAlbum.builder().userId(userId).videoAlbumId(videoAlbumId).build());
        }
        boolean aBoolean = userService.deleteUserById(userId);
        if (aBoolean) {
            return ResponseEntity.ok(userId);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/videos")                 //  根据条件获取视频
    @Operation(summary = "根据条件获取视频")
    public ResponseEntity<?> getVideos(@RequestBody Video video) {
        List<Video> videos = videoService.findVideos(video);
        if (!videos.isEmpty()) {
            return ResponseEntity.ok(videos);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/updateVideo")            //  更新视频审核状态
    @Operation(summary = "更新视频")
    public ResponseEntity<?> updateVideo(@RequestBody Video video) {
        video.setVideoPath(null);
        video.setThumbnailPath(null);
        boolean aBoolean = videoService.updateVideo(video);
        if (aBoolean) {
            return ResponseEntity.ok(video);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/Albums")
    @Operation(summary = "根据条件获取专辑")
    public ResponseEntity<?> getAlbums(@RequestBody VideoAlbum videoAlbum) {
        List<VideoAlbum> videoAlbumList = videoAlbumService.getVideoAlbums(videoAlbum);
        if (!videoAlbumList.isEmpty()) {
            return ResponseEntity.ok(videoAlbumList);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/updateAlbum")
    @Operation(summary = "更新专辑")
    public ResponseEntity<?> updateAlbum(@RequestBody VideoAlbum videoAlbum) {
        //  禁止设置海报的路径
        videoAlbum.setVideoPostPath(null);
        boolean result = videoAlbumService.updateVideoAlbum(videoAlbum);
        if (result) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

}
