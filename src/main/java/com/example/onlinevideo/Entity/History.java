package com.example.onlinevideo.Entity;

import lombok.Data;

@Data
public class History {
    private Integer historyId;
    private Integer userId;
    private Integer videoId;
    private Integer watchedSeconds;
    private long timestamp;

    private String videoName;
    private Integer videoIsVip;
    private String videoTitle;
    private Integer duration;
    private String videoPostPath;   //  或许弃用，thumbnail代替
    private String thumbnailPath;
}
