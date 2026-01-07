package com.example.onlinevideo.Entity;

import com.example.onlinevideo.DTO.Page;
import com.example.onlinevideo.Enum.VideoApprovalStatus;
import com.example.onlinevideo.Enum.VideoType;
import jakarta.persistence.*;
import lombok.Data;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
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
    private VideoApprovalStatus videoApprovalStatus;
//    @Indexed
    private String videoName;
    private String videoPath;
    private String videoTitle;
    private Integer viewCount;
    private Integer duration;
    private String thumbnailPath;
    /**
     * 视频类型：免费、收费、VIP、无版权、独播
     */
    private VideoType videoType;
    /**
     * 源视频链接（当举报类型为侵权时使用）
     */
    private String sourceVideoUrl;
    /**
     * 兼容旧字段，已废弃，使用videoType代替
     * @deprecated 使用 videoType 代替
     */
    @Deprecated
    private Integer videoIsVip;
//    @ManyToOne
//    private VideoAlbum videoAlbum;

}