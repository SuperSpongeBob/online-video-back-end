package com.example.onlinevideo.Task;

import com.example.onlinevideo.Service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SyncTask {
    @Autowired
    private VideoService videoService;

    //  每隔一分钟执行一次
    @Scheduled(fixedRate = 60000)
    public void syncViewCountToDatabase(){
        videoService.syncViewCountToDatabase();
    }
}
