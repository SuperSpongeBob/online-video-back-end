package com.example.onlinevideo.Entity;

import com.example.onlinevideo.DTO.Page;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@Entity(name = "comment")
@AllArgsConstructor
@NoArgsConstructor
public class VideoComment extends Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer videoCommentId;
    private String videoCommentContent;
    private long videoCommentTime;
    private Integer userId;
    private Integer videoId;
    private String userName;
    private Integer parentId;       //  父评论ID（新增）
    private Integer replyToId;      //  回复目标用户ID（新增）

    @Transient
    private List<VideoComment> replies;
}
