package com.example.onlinevideo.Entity;

import com.example.onlinevideo.DTO.Page;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@Entity(name = "comment")
@AllArgsConstructor
@NoArgsConstructor
public class VideoComment  extends Page {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer videoCommentId;
    private String videoCommentContent;
    private long videoCommentTime;
    private Integer userId;
    private Integer videoId;
    private String userName;
}
