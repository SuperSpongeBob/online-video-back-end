package com.example.onlinevideo.Service;

import com.example.onlinevideo.Mapper.VideoAlbumMapper;
import com.example.onlinevideo.Entity.VideoAlbum;
import com.example.onlinevideo.Mapper.VideoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VideoAlbumService {
    @Autowired
    private VideoAlbumMapper videoAlbumMapper;
    @Autowired
    private FileService fileService;
    @Autowired
    private VideoMapper videoMapper;

    //  OwnershipCheckAspect 中用到，不能删
    public Integer getOwnerId(Integer videoAlbumId){
        return videoAlbumMapper.findOwnerIdByAlbumId(videoAlbumId);
    }

    //  可根据videoAlbumId、userId获取专辑信息
    public List<VideoAlbum> getVideoAlbums(VideoAlbum videoAlbum) {
        return videoAlbumMapper.getVideoAlbums(videoAlbum);
    }

    /**
     *
     * @param videoAlbum
     * @return true 删除成功
     * @return false 删除失败
     */
    //  删除专辑时，他的海报、视频、缩略图等一并删除
    public boolean deleteAlbum(VideoAlbum videoAlbum) {
        try {
            //  先根据专辑Id查找视频信息
            Optional<VideoAlbum> album = videoAlbumMapper.getAlbumByVideoAlbumId(videoAlbum.getVideoAlbumId());
            if (album.isPresent()) {
                //  根据videoAlbumId查询所有videoId
                List<Integer> videoIds =videoMapper.getVideoIdsByAlbumId(videoAlbum.getVideoAlbumId());
                if (!videoIds.isEmpty()) {
                    //  根据 videoId查询所有视频文件路径
                    List<String> videoPaths=videoMapper.getVideoPathsByVideoIds(videoIds);
                    for (String videoPath : videoPaths) {
                        System.out.println("删除： " + videoPath);
                        //  删除视频文件
                        fileService.deleteVideoFile(videoPath);
                    }
                    //  根据videoId查询所有的缩略图文件路径
                    List<String> thumbnailPaths=videoMapper.getThumbnailPathsByVideoIds(videoIds);
                    for (String thumbnailPath : thumbnailPaths) {
                        System.out.println("删除： " + thumbnailPath);
                        //  删除缩略图
                        fileService.deleteImagesFile(thumbnailPath);
                    }
                    //  删除数据库中的视频信息
                    videoMapper.deleteVideosByVideoIds(videoIds);
                }
                //  删除专辑海报
                fileService.deleteImagesFile(album.get().getVideoPostPath());

                //  最后删除数据库中的信息
                return videoAlbumMapper.deleteAlbum(videoAlbum);    //  即使文件没有删除成功也将数据库中的数据删除
            }
            return false;
        }catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //  admin更新专辑
    public boolean updateVideoAlbum(VideoAlbum videoAlbum) {
        return videoAlbumMapper.updateVideoAlbum(videoAlbum);
    }

    public boolean upsertVideoAlbum(VideoAlbum videoAlbum) {
        return videoAlbumMapper.upsertVideoAlbum(videoAlbum);
    }

    //  删除海报
    public boolean deleteVideoPost(Integer videoAlbumId) {
        Optional<VideoAlbum> album = videoAlbumMapper.getAlbumByVideoAlbumId(videoAlbumId);
        if (album.isPresent()) {
            fileService.deleteImagesFile(album.get().getVideoPostPath());
            return true;
        }
        return false;
    }

}


















