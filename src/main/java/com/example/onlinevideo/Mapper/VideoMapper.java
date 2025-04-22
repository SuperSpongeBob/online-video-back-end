package com.example.onlinevideo.Mapper;

import com.example.onlinevideo.Entity.Video;
import com.example.onlinevideo.Entity.VideoAlbum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface VideoMapper {
    //验证视频是否为VIP
    Integer verifyVideo(String string);

    //  根据videoId查找videoPath    获取ids用于批量删除文件
    String getVideoPathByVideoId(String videoId);
    List<String> getVideoPathsByVideoIds(List<Integer> videoIds);
    List<String> getThumbnailPathsByVideoIds(List<Integer> videoIds);
    void deleteVideosByVideoIds(List<Integer> videoIds);

    //  根据albumId获取videoId，用于删除
    List<Integer> getVideoIdsByAlbumId(Integer videoAlbumId);

    //  集成根据各种需求返回信息
    List<Video> findVideos(Video video);

    //  分页查找所有视频信息
    List<Video> getIndexVideo(Video video);

    //  根据id查找视频
    Optional<Video> findVideoByVideoId(Integer videoId);

    //  用户重申视频，将禁播改为待审核
    boolean reiterateVideo(Integer videoId);

    //  搜索视频
    List<Video> SearchVideo(Video video);

    //  上传视频
    int insertVideo(Video video);

    //  admin更新视频状态、审核视频
    boolean updateVideo(Video video);

    // 删除视频
    boolean deleteVideo(Integer videoId);

    //  验证视频是否存在
    boolean existsVideo(Video video);

    //  增加视频播放量
    void increaseViewCount(Integer videoId, Integer viewCount);

    //  推荐的视频搜索
    List<Video> selectNewestVideos(@Param("limit") int limit);
    List<Video> selectHottestVideos(@Param("limit") int limit);
    List<Video> selectVideosOrderByIdDesc(@Param("offset") int offset, @Param("limit") int limit);
}
