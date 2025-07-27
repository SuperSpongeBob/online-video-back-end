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

    //  查询视频下的所有的顶级评论（parentId为null）
    List<VideoComment> getTopVideoComments(VideoComment videoComment);

    //  查询指定评论的所有回复
    List<VideoComment> getRepliesByParentId(VideoComment videoComment);

    //  根据Id删除评论及其所有回复（级联删除）
    boolean deleteCommentAndReplies(@Param("videoCommentId") Integer videoCommentId);
}
