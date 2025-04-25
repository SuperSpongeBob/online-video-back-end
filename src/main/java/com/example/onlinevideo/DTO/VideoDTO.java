package com.example.onlinevideo.DTO;

import lombok.Data;

@Data
public class VideoDTO {
    private Integer videoId;
    private Integer videoAlbumId;
    private String videoApprovalStatus;
    private String videoName;
    private String videoPath;
    private String videoTitle;
    private Integer viewCount;
    private Integer duration;
    private String thumbnailPath;
    private Integer videoIsVip;
    private String videoChannel;
}
