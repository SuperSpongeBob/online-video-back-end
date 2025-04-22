package com.example.onlinevideo.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor  // 无参构造器
@AllArgsConstructor  // 全参构造器
public class Danmaku {
    private Integer danmakuId;         //  弹幕id
    private Integer userId;     //  关联的用户Id
    private Integer videoId;    //  关联的视频id
    private String text;        //  弹幕内容
    private Float time;         //  弹幕出现的视频时间点，单位：秒
}
