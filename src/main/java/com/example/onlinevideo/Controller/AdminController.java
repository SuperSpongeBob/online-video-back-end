package com.example.onlinevideo.Controller;

import com.example.onlinevideo.Annotation.RateLimit;
import com.example.onlinevideo.DTO.RegisterRequest;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
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
    public ResponseEntity<?> getUsers(@RequestBody User user,
                                      @RequestParam(required = false) Long startTime,
                                      @RequestParam(required = false) Long endTime) {
        List<User> users = userService.getUsers(user, startTime, endTime);      //  获取user表中的所有用户信息
        //  遍历赋值
        for (User user1 : users) {
            //  根据userId获取roleId赋值到identity传到前端以展示身份
            UserRole userRole = userRoleService.getRoleIdByUserId(user1.getUserId());
            if (userRole != null) {
                user1.setIdentity(userRole.getRoleId());
            }
        }
        if (!users.isEmpty()) {
            return ResponseEntity.ok(users);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/insertUser")
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
    public R updateUser(@RequestBody User user) {
        UserRole userRole = new UserRole();     //  new对象
        userRole.setUserId(user.getUserId());   //  赋值到userRole中
        userRole.setRoleId(user.getIdentity()); //  将前端修改的身份赋值到userRole中作为他的权限
        userRoleService.updateUserRole(userRole);   //  修改用户权限

        return userService.updateUser(user);   //  修改用户其他信息
    }

    @DeleteMapping("/deleteUser/{userId}")      //  根据用户id删除用户
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
    public ResponseEntity<?> getVideos(@RequestBody Video video) {
        List<Video> videos = videoService.findVideos(video);
        if (!videos.isEmpty()) {
            return ResponseEntity.ok(videos);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/updateVideo")            //  更新视频审核状态
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
    public ResponseEntity<?> getAlbums(@RequestBody VideoAlbum videoAlbum) {
        List<VideoAlbum> videoAlbumList = videoAlbumService.getVideoAlbums(videoAlbum);
        if (!videoAlbumList.isEmpty()) {
            return ResponseEntity.ok(videoAlbumList);
        } else {
            return ResponseEntity.noContent().build();
        }
    }

    @PostMapping("/updateAlbum")
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
