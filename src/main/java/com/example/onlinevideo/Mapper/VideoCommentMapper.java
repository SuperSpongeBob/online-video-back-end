package com.example.onlinevideo.Mapper;

import com.example.onlinevideo.Entity.VideoComment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoCommentMapper {
    //根据视频id获取评论
    List<VideoComment> getVideoCommentsByVideoId(VideoComment videoComment);

    Boolean addVideoComment(@Param("Comment") VideoComment videoComment);

    //  根据条件删除评论
    boolean deleteComment(VideoComment videoComment);
}
