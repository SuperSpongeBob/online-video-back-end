package com.example.onlinevideo.Mapper;

import com.example.onlinevideo.Entity.Danmaku;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DanmakuMapper {
    boolean insertDanmaku(Danmaku danmaku);

    List<Danmaku> getDanmaku(Danmaku danmaku);

    boolean deleteDanmaku(Danmaku danmaku);
}
