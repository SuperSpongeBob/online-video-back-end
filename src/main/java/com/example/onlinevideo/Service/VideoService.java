package com.example.onlinevideo.Service;

import com.example.onlinevideo.Entity.VideoComment;
import com.example.onlinevideo.Mapper.UserMapper;
import com.example.onlinevideo.Mapper.VideoMapper;
import com.example.onlinevideo.Entity.Danmaku;
import com.example.onlinevideo.Entity.Video;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class VideoService {

    @Autowired
    private VideoMapper videoMapper;
    @Autowired
    private FileService fileService;
    @Autowired
    private DanmakuService danmakuService;
    @Autowired
    private VideoCommentService videoCommentService;
    @Autowired
    private RedisTemplate<String, Integer> redisTemplate;

    @Value("${jwt.secret}")
    private String SECRET_KEY;
    @Autowired
    private UserMapper userMapper;

    /**
     * @param videoId
     * @return false 代表视频免费，放行； ture  代表用户权限不足，禁止观看
     */
    public boolean verifyVideo(String videoId, String token) {
        Integer videoIsVip = videoMapper.verifyVideo(videoId);   //  根据视频id获取该视频的全部信息

        if (videoIsVip.equals(1)) {
            return false;                   //  1即为免费，false放行，不进行权限校验
        }

        if (token != null && !token.isEmpty()) {
            //  解析token获取userId
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token.replace("Bearer ", ""))
                    .getBody();

            if (videoIsVip.equals(2)) {
                //  获取用户角色
                List<String> roles = (List<String>) claims.get("roles");

                //  判断用户角色，如果为 admin 或 VIP 则可以观看
                if (roles.contains("ROLE_ADMIN") || roles.contains("ROLE_VIP")) {
                    return false;
                }
            }

            //  从token中解析出当前登录的用户Id
            Integer userId = claims.get("userId", Integer.class);

            //  如果当前视频为当前用户所发布，可以进行观看
            //  判断该视频是否为用户所发布
            return userId == null || !userId.equals(userMapper.getUserIdByVideoId(Integer.parseInt(videoId)));
        }
        return true;   //   用户既不是VIP，也不是视频的发布者
    }

    public String getVideoPathByVideoId(String videoId) {
        return videoMapper.getVideoPathByVideoId(videoId);
    }

    //集成于根据id、name、status、page分页查找
    public List<Video> findVideos(Video video) {
        return videoMapper.findVideos(video);

    }

    //根据id查找视频
    public Optional<Video> findVideoByVideoId(Integer videoId) {
        return videoMapper.findVideoByVideoId(videoId);
//        return videoRepository.findById(id);
    }

    //分页查找所有视频,集成于根据id、name、status、page分页查找
    public List<Video> getIndexVideo(Video video) {
        return videoMapper.getIndexVideo(video);
    }


    public List<Video> SearchVideo(Video video) {
        return videoMapper.SearchVideo(video);
    }

    //  插入视频信息
    public int insertVideo(Video video) {
        return videoMapper.insertVideo(video);
    }

    //  admin更新视频信息、审核视频
//    @Transactional
    public boolean updateVideo(Video video) {
        return videoMapper.updateVideo(video);
    }

    //  删除视频
    public boolean deleteVideo(Integer videoId) {
        try {
            //  先根据视频id查找视频信息
            Optional<Video> video = videoMapper.findVideoByVideoId(videoId);
            if (video.isPresent()) {
                //  删除视频文件、缩略图
                fileService.deleteImagesFile(video.get().getThumbnailPath());
                boolean isDeleted = fileService.deleteVideoFile(video.get().getVideoPath());
                if (!isDeleted) {
                    System.out.println("文件删除失败或文件不存在");
                }
                //  删除该视频下的弹幕
                danmakuService.deleteDanmaku(Danmaku.builder().videoId(videoId).build());
                //  删除该视频下的评论
                videoCommentService.deleteComment(VideoComment.builder().videoId(videoId).build());

                return videoMapper.deleteVideo(videoId);    //  即使文件没有删除成功也将数据库数据删除
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //  删除缩略图
    public boolean deleteThumbnail(Integer videoId) {
        Optional<Video> video = videoMapper.findVideoByVideoId(videoId);
        if (video.isPresent()) {
            boolean isDeleted = fileService.deleteImagesFile(video.get().getThumbnailPath());
            if (!isDeleted) {
                System.out.println("缩略图删除失败或文件不存在");
            }
        }
        return true;
    }

    public boolean existsVideo(Video video) {
        return videoMapper.existsVideo(video);
    }

    //  视频播放量
    public void increaseViewCount(Integer videoId) {
        String redisKey = "view_count:video:" + videoId;
        //  现在 Redis 中增加播放量
        redisTemplate.opsForValue().increment(redisKey, 1);
        //  设置一个过期时间，到期后自动将 Redis 中的数据更新到数据库
        redisTemplate.expire(redisKey, 1, TimeUnit.MINUTES);
    }

    //  定期将 Redis 中的数据更新到数据库
    public void syncViewCountToDatabase() {
        //  遍历 Redis 中的所有视频播放量数据
        for (String key : Objects.requireNonNull(redisTemplate.keys("view_count:video:*"))) {
            Integer videoId = Integer.parseInt(key.split(":")[2]);
            Integer viewCount = redisTemplate.opsForValue().get(key);
            if (viewCount != null && viewCount > 0) {

                //  直接传入从 Redis 获取的播放量值进行累加
                videoMapper.increaseViewCount(videoId, viewCount);

                //  更新完后删除 Redis 中的数据
                redisTemplate.delete(key);
            }
        }
    }
}
