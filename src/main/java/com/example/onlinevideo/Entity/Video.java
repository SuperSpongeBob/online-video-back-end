package com.example.onlinevideo.Entity;

import com.example.onlinevideo.DTO.Page;
import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;

@Data
@Entity
//@Table(name = "video")
public class Video extends Page {
//    private static final long serialVersionUID = 1L;
    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer videoId;
//    @Indexed
    private Integer videoAlbumId;
    private String videoApprovalStatus;
//    @Indexed
    private String videoName;
    private String videoPath;
    private String videoTitle;
    private Integer viewCount;
    private Integer duration;
    private String thumbnailPath;
    private Integer videoIsVip;
//    @ManyToOne
//    private VideoAlbum videoAlbum;

}