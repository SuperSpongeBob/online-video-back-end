package com.example.onlinevideo.Service;

import com.example.onlinevideo.DTO.VideoDTO;
import com.example.onlinevideo.Entity.VideoComment;
import com.example.onlinevideo.Enum.VideoType;
import com.example.onlinevideo.Mapper.UserMapper;
import com.example.onlinevideo.Mapper.VideoMapper;
import com.example.onlinevideo.Entity.Danmaku;
import com.example.onlinevideo.Entity.Video;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
public class VideoService {

    private static final Logger log = LoggerFactory.getLogger(VideoService.class);
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
     * @return false 代表视频免费，放行； true  代表用户权限不足，禁止观看
     */
    public boolean verifyVideo(String videoId, String token) {
        Integer videoTypeCode = videoMapper.verifyVideo(videoId);   //  根据视频id获取视频类型
        
        if (videoTypeCode == null) {
            return true; // 视频不存在，禁止观看
        }

        //  解析token获取userId
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token.replace("Bearer ", ""))
                .getBody();

        //  获取用户角色
        List<String> roles = (List<String>) claims.get("roles");

        if (roles.contains("ROLE_ADMIN")) {
            return false;
        }

        
        VideoType videoType = VideoType.fromCode(videoTypeCode);
        
        // 免费视频直接放行
        if (videoType == VideoType.FREE) {
            return false;
        }
        
        // 无版权视频允许观看30秒
        if (videoType == VideoType.NO_COPYRIGHT) {
            return true;
        }

        if (token != null && !token.isEmpty()) {

            // VIP视频需要VIP或管理员权限
            if (videoType == VideoType.VIP) {

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
    
    /**
     * 获取视频类型和源视频链接信息
     * @param videoId 视频ID
     * @return Map包含videoType和sourceVideoUrl，videoType为枚举名称（如NO_COPYRIGHT）
     */
    public Map<String, Object> getVideoTypeAndSourceUrl(Integer videoId) {
        Map<String, Object> result = videoMapper.getVideoTypeAndSourceUrl(videoId);
        if (result != null && result.containsKey("videoType")) {
            Object videoTypeObj = result.get("videoType");
            if (videoTypeObj != null) {
                // 将数字代码转换为枚举名称
                Integer videoTypeCode = null;
                
                // 处理各种可能的类型
                if (videoTypeObj instanceof Integer) {
                    videoTypeCode = (Integer) videoTypeObj;
                } else if (videoTypeObj instanceof Boolean) {
                    // MyBatis 可能将 tinyint 映射为 Boolean（0=false, 非0=true）
                    // 这种情况下，我们需要重新查询获取原始值
                    log.warn("videoType 被映射为 Boolean 类型，重新查询原始值");
                    Integer rawCode = videoMapper.verifyVideo(videoId.toString());
                    if (rawCode != null) {
                        videoTypeCode = rawCode;
                    }
                } else if (videoTypeObj instanceof Number) {
                    videoTypeCode = ((Number) videoTypeObj).intValue();
                } else if (videoTypeObj instanceof String) {
                    try {
                        videoTypeCode = Integer.parseInt((String) videoTypeObj);
                    } catch (NumberFormatException e) {
                        // 如果已经是字符串格式（枚举名称），直接使用
                        return result;
                    }
                } else if (videoTypeObj instanceof VideoType) {
                    // 如果已经是枚举类型，直接获取名称
                    result.put("videoType", ((VideoType) videoTypeObj).name());
                    return result;
                }
                
                if (videoTypeCode != null) {
                    try {
                        VideoType videoType = VideoType.fromCode(videoTypeCode);
                        // 返回枚举名称（如 NO_COPYRIGHT）
                        result.put("videoType", videoType.name());
                    } catch (IllegalArgumentException e) {
                        // 如果无法转换，保持原值
                        log.warn("无法转换视频类型代码: {}", videoTypeCode);
                    }
                }
            }
        }
        return result;
    }

    public String getVideoPathByVideoId(String videoId) {
        return videoMapper.getVideoPathByVideoId(videoId);
    }

    //集成于根据id、name、status、page分页查找
    public List<Video> findVideos(Video video) {
        return videoMapper.findVideos(video);
    }

    //  根据专辑Id查找视频
    public List<VideoDTO> videosInSameAlbum(Integer videoId){
        return videoMapper.videosInSameAlbum(videoId);
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

    public List<Video> videosByUserId(Integer userId) {
        return videoMapper.videosByUserId(userId);
    }


    public List<Video> SearchVideo(Video video) {
        return videoMapper.SearchVideo(video);
    }

    //  插入视频信息
    public int insertVideo(Video video) {
        return videoMapper.insertVideo(video);
    }

    //  admin更新视频信息、审核视频
    @Transactional
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
                fileService.deleteVideoFile("preview_" + video.get().getVideoPath());
                if (!isDeleted) {
                    log.error("视频文件删除失败", video.get().getVideoPath());
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
                log.error("缩略图删除失败", video.get().getThumbnailPath());
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
