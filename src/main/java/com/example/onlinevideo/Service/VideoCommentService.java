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
}
