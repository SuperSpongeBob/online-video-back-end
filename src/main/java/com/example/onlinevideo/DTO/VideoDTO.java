package com.example.onlinevideo.DTO;

import com.example.onlinevideo.Enum.VideoApprovalStatus;
import com.example.onlinevideo.Enum.VideoType;
import lombok.Data;

@Data
public class VideoDTO {
    private Integer videoId;
    private Integer videoAlbumId;
    private VideoApprovalStatus videoApprovalStatus;
    private String videoName;
    private String videoPath;
    private String videoTitle;
    private Integer viewCount;
    private Integer duration;
    private String thumbnailPath;
    private VideoType videoType;
    private String sourceVideoUrl;
    /**
     * 兼容旧字段，已废弃，使用videoType代替
     * @deprecated 使用 videoType 代替
     */
    @Deprecated
    private Integer videoIsVip;
    private String videoChannel;
    private String VideoAlbumName;
}
