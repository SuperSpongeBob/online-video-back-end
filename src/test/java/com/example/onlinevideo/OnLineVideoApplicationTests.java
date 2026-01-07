package com.example.onlinevideo;

import com.example.onlinevideo.Controller.VideoAlbumController;
import com.example.onlinevideo.DTO.VideoDTO;
import com.example.onlinevideo.Enum.VideoApprovalStatus;
import com.example.onlinevideo.Mapper.*;
import com.example.onlinevideo.Entity.*;
import com.example.onlinevideo.Security.CustomUserDetails;
import com.example.onlinevideo.Security.JwtTokenProvider;
import com.example.onlinevideo.Service.*;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.core.Authentication;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@SpringBootTest
@EnableCaching
class OnLineVideoApplicationTests {
    @Autowired
    private VideoCommentMapper videoCommentMapper;
    @Autowired
    private VideoCommentService videoCommentService;
    @Autowired
    private VideoService videoService;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private VideoMapper videoMapper;
    @Autowired
    private VideoAlbumMapper videoAlbumMapper;
    @Autowired
    private VideoAlbumService videoAlbumService;
    @Autowired
    private VideoAlbumController videoAlbumController;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRoleService userRoleService;
    @Autowired
    private FileService fileService;
    @Autowired
    private HistoryService historyService;
    @Autowired
    private HistoryMapper historyMapper;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.secret}")
    private String SECRET_KEY;

    @Test
    void videoByAlbumId(){
    List<VideoDTO> videoDTOList = videoService.videosInSameAlbum(48);
        System.out.println(videoDTOList);
    }

    @Test
    public void deleteAlbum(){
        Optional<VideoAlbum> album = videoAlbumMapper.getAlbumByVideoAlbumId(21);
        if(album.isPresent()){
            System.out.println(album);
        }
        List<Integer> videoIds = videoMapper.getVideoIdsByAlbumId(25);
        System.out.println(videoIds);
        List<String> videoPaths = videoMapper.getVideoPathsByVideoIds(videoIds);
        System.out.println(videoPaths);
        List<String> thumbnailPaths = videoMapper.getThumbnailPathsByVideoIds(videoIds);
        System.out.println(thumbnailPaths);
        List<Integer> userIds = Arrays.asList(46, 47, 48); // 假设查询多个用户的albumIds
        List<Integer> videoAlbumIds = videoAlbumMapper.findAlbumIdsByUserId(46);
        System.out.println(videoAlbumIds);

    }

    @Test
    public void token(){
        String token = "eyJhbGciOiJIUzUxMiJ9.eyJyb2xlcyI6WyJST0xFX0FETUlOIl0sInVzZXJQaG9uZSI6IjE4NjEzMTYwMDAwIiwidXNlcklkIjo0NiwiaWF0IjoxNzQ0ODgwNzM0LCJleHAiOjE3NDQ5NjcxMzR9.u7FRYuvRVJbcDacvf1ngPWUatdGIngOdq3jFBZ7LdDyZIZ98quS1XQ8LMWUv0p4e4Giu0m5JsRR3OOdDV80wNw";
        String userPhone = jwtTokenProvider.getUsernameFromToken(token);
        System.out.println(userPhone);
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        System.out.println(authentication.getPrincipal());
        System.out.println(authentication.getAuthorities());
        System.out.println(authentication.getCredentials());
        System.out.println(authentication.getDetails());
        System.out.println(authentication.getName());
    }

    @Test
    public void getIndexVideos(){
        Video video = new Video();
        VideoAlbum videoAlbum = new VideoAlbum();
//        video.setVideoAlbum(videoAlbum);
//        video.setVideoId(50);
        System.out.println(videoMapper.getIndexVideo(video));
    }



    @Test
    public void detailedHistory(){
        History history = new History();
        history.setVideoId(38);
        List<History> historyList = historyMapper.detailHistory(history);
        System.out.println(historyList);
    }

    @Test
    public void getHistory(){
        History history = new History();
//        history.setUserId(46);
//        history.setVideoId(38);
        History historyList = historyService.historyByHistoryId(history);
        System.out.println(historyList);
    }

    @Test
    public void insertHistory(){
        History history = new History();
        history.setUserId(46);
        history.setVideoId(38);
        history.setWatchedSeconds(5);
        boolean success = historyService.insertHistory(history);
        System.out.println(success);
    }

    @Test
    public void existsVideo(){
        Video video = new Video();
        video.setVideoId(441);
        boolean exist = videoService.existsVideo(video);
        System.out.println(exist);
    }

    @Test
    public void getAlbums(){
        VideoAlbum videoAlbum = new VideoAlbum();
        List<VideoAlbum> videoAlbumList=videoAlbumService.getVideoAlbums(videoAlbum);
        System.out.println(videoAlbumList.size());
    }

    @Test
    public void getRoleIdByUserId(){
        UserRole userRole = userRoleService.getRoleIdByUserId(4);
        System.out.println(userRole);

    }

    @Test
    public void findVideoByVideoId(){
        Optional<Video> video = videoService.findVideoByVideoId(1);
        System.out.println(video);
    }

    @Test
    public void verifyVideo(){
//        boolean verify= videoService.verifyVideo("78",46);
//        System.out.println(verify);
    }


    @Test
    public void updateVideo(){
        Video video = new Video();
        video.setVideoId(6);
//        video.setVideoApprovalStatus(VideoApprovalStatus.REVIEW_PASSED);
        video.setVideoApprovalStatus(VideoApprovalStatus.PENDING_REVIEW);
        System.out.println(videoService.updateVideo(video));
    }

    @Test
    public void updateUser() throws Exception {
        User user = new User();
//        user.setUserIsVip(0);
//        user.setUserCredibility("");
        user.setUserId(2);
//        System.out.println(userMapper.updateUser(user));
    }

    @Test
    public void getUsers(){
        User user = new User();
        user.setUserId(2);
//        user.setUserIsVip(1);
        user.setUserName("测试");
//        user.setUserNickname("test");
        user.setUserGender("男");
//        List<User> users=userService.getUsers(user);
//        System.out.println(users);
    }



    @Test
    public void getVAByUserId(){
        VideoAlbum videoAlbum = new VideoAlbum();
        videoAlbum.setUserId(41);
        System.out.println(videoAlbumController.getVideoAlbumsByUserId(41));
//        System.out.println(videoAlbumMapper.getVideoAlbumsByUserId(videoAlbum));
    }

    @Test
    public void addVideoAlbum(){
        VideoAlbum videoAlbum = new VideoAlbum();
        videoAlbum.setVideoAlbumName("视频专辑名");
//        videoAlbum.setVideoAddUser("上传用户");
        videoAlbum.setVideoReleaseDate("2024/10/12");
        videoAlbum.setVideoActor("演员");
//        System.out.println(videoAlbumService.addVideoAlbum(videoAlbum));
        System.out.println(videoAlbum.getVideoAlbumId());
    }

    @Test
    public void getVideoAlbum(){
        VideoAlbum videoAlbum = new VideoAlbum();
        videoAlbum.setPageNum(1);
        videoAlbum.setPageSize(3);
//        videoAlbum.setVideoAlbumName("遮天");
//        videoAlbum.setVideoAlbumId(1);
//        videoAlbum.setUserId(41);
        System.out.println(videoAlbumService.getVideoAlbums(videoAlbum).size());
    }


    @Test
    public void userIsVip(){
        User user = new User();
        user.setUserPhone("17806573192");
        System.out.println(user);
    }

    @Test
    public void updateUserPassword() {
        User user = new User();
        user.setUserId(2);
        user.setUserPassword("321");
//        user.setUserNewPassword("123");
//        boolean s=userMapper.updateUserPassword(user);
//        System.out.println(s);
    }

    @Test
    public void findVideos() {
        Video video = new Video();
//        video.setVideoApprovalStatus(VideoApprovalStatus.REVIEW_PASSED);
        video.setPageNum(1);
        video.setPageSize(33);
//        video.setVideoId(2);
//        video.setVideoIsVip(1);
//        video.setVideoTitle("test");
//        video.setVideoName("遮天");
        VideoAlbum videoAlbum = new VideoAlbum();
//        videoAlbum.setVideoAlbumId(2);
        videoAlbum.setUserId(2);
//        videoAlbum.setVideoChannel("动漫");
//        video.setVideoAlbum(videoAlbum);

        List<Video> videos = videoService.findVideos(video);
        System.out.println(videos);
        for (Video v : videos) {
//            System.out.println(v.getVideoId()+":"+v.getVideoAlbum().getUserId());
        }
//        video.setVideoName("8");
//        List<Video> videos1 = videoService.findVideos(video);
//        for (Video v : videos1) {
//            System.out.println(videos1);
//        }
    }

    @Test
    public void searchVideo() {
        Video video = new Video();
        video.setVideoName("遮天");
//        video.setId(2);
        List<Video> videos = videoService.SearchVideo(video);
        System.out.println(videos.size());
    }

    @Test
    public void getIndexVideo() {
        Video video = new Video();
//        video.setPageSize(5);
//        video.setPageNum(1);
        VideoAlbum videoAlbum = new VideoAlbum();
//        videoAlbum.setUserId(41);
//        videoAlbum.setVideoAlbumId(14);
//        videoAlbum.setVideoChannel("动漫");
//        video.setVideoAlbum(videoAlbum);
//        video.getVideoAlbum().setUserId(41);
//        video.setVideoApprovalStatus(VideoApprovalStatus.REVIEW_PASSED);
//        video.setVideoName("遮天");
        List<Video> videos = videoService.getIndexVideo(video);
//        System.out.println(videos);
/*        for (Video v : videos) {
//            System.out.println(v.getVideoAlbum());
            if (v.getVideoAlbum()!=null) {
                System.out.println(v.getVideoId());
//                System.out.println(v.getVideoAlbum().getVideoPostPath());
            }else {
                System.out.println("VideoAlbum is null for videoId: " + v.getVideoId());
            }
        }*/

    }

    @Test
    public void getVideoComments() {
        VideoComment videoComment = new VideoComment();
        videoComment.setPageNum(1);
        videoComment.setPageSize(6);
        videoComment.setVideoId(2);
        List<VideoComment> videoComments = videoCommentService.getVideoCommentsByVideoId(videoComment);
        System.out.println(videoComment);
        System.out.println(videoComments);
    }

    @Test
    public void insertVideoComment() {
        VideoComment videoComment = new VideoComment();
        videoComment.setVideoCommentContent("测试内容");
        videoComment.setVideoId(10);
        videoComment.setUserId(2);
//        videoComment.setVideoCommentTime("2023-08-18 16:02:42");
        System.out.println(videoComment);
        System.out.println(videoCommentMapper.addVideoComment(videoComment));
    }


    @Test
    void contextLoads() {
    }

}










































