package com.example.onlinevideo.Service;

import com.example.onlinevideo.Mapper.DanmakuMapper;
import com.example.onlinevideo.Entity.Danmaku;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DanmakuService {
    @Autowired
    private DanmakuMapper danmakuMapper;

    public boolean addDanmaku(Danmaku danmaku) {
        return danmakuMapper.insertDanmaku(danmaku);
    }
    public List<Danmaku> getDanmakus(Danmaku danmaku) {
        return danmakuMapper.getDanmaku(danmaku);
    }

    public boolean deleteDanmaku(Danmaku danmaku) {
        return danmakuMapper.deleteDanmaku(danmaku);
    }
}
