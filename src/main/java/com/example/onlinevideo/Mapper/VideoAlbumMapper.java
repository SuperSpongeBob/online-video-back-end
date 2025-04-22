package com.example.onlinevideo.Mapper;

import com.example.onlinevideo.Entity.VideoAlbum;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;

import java.util.List;
import java.util.Optional;

@Mapper
public interface VideoAlbumMapper {
    //  根据 videoAlbumId 获取 userId   OwnershipCheckAspect 中调用，不能删
    Integer findOwnerIdByAlbumId(Integer videoAlbumId);

    List<Integer> findAlbumIdsByUserId(Integer userId);

    //  获取专辑信息
    List<VideoAlbum> getVideoAlbums(VideoAlbum videoAlbum);

    //  根据用户id获取专辑信息
    Optional<VideoAlbum> getAlbumByVideoAlbumId(Integer videoAlbumId);

    //  admin更新专辑
    boolean updateVideoAlbum(VideoAlbum videoAlbum);

    //  数据库中数据不存在时执行插入操作，存在时执行更新操作，性能消耗比纯update略大，而且高并发时可能会引起锁争用，只用于专辑视频上传模块
    @Options(useGeneratedKeys = true,keyProperty = "videoAlbumId")
    boolean upsertVideoAlbum(VideoAlbum videoAlbum);

    //  删除专辑
    boolean deleteAlbum(VideoAlbum videoAlbum);
}
