package com.example.onlinevideo.Service;

import com.example.onlinevideo.Mapper.VideoCommentMapper;
import com.example.onlinevideo.Entity.VideoComment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VideoCommentService {
    @Autowired
    private VideoCommentMapper videoCommentMapper;

    public List<VideoComment> getVideoCommentsByVideoId(VideoComment videoComment) {
        return videoCommentMapper.getVideoCommentsByVideoId(videoComment);
    }

    public Boolean addVideoComment(VideoComment videoComment) {
        return videoCommentMapper.addVideoComment(videoComment);
    }

    public boolean deleteComment(VideoComment videoComment) {
        return videoCommentMapper.deleteComment(videoComment);
    }


    //  获取视频下的所有评论（含嵌套回复）
    public List<VideoComment> getVideoCommentsWithReplies(VideoComment videoComment) {
        //  获取顶级评论
        List<VideoComment> topComments = videoCommentMapper.getTopVideoComments(videoComment);

        //  递归加载所有回复
        for (VideoComment comment : topComments){
            loadRepliesRecursively(comment);
        }
        return topComments;
    }

    // 递归加载评论的所有回复
    private void loadRepliesRecursively(VideoComment comment) {
        System.out.println("comment: "+comment);
        List<VideoComment> replies = videoCommentMapper.getRepliesByParentId(comment);
        System.err.println("replies：" + replies);
        if (!replies.isEmpty()) {
            comment.setReplies(replies);
            for (VideoComment reply : replies) {
                loadRepliesRecursively(reply);
            }
        }
        System.err.println("comment："+comment);
    }

    //  删除评论及其所有回复
    public boolean deleteCommentWithReplies(Integer videoCommentId) {
        return videoCommentMapper.deleteCommentAndReplies(videoCommentId);
    }
}
