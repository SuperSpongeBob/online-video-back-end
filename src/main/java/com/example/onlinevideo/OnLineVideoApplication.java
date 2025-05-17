package com.example.onlinevideo;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.io.File;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class OnLineVideoApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnLineVideoApplication.class, args);
    }

    @PostConstruct
    public void init() {
        // 确保日志目录存在
        new File("/var/log/online-video").mkdirs();
        new File("/var/log/online-video/console").mkdirs();
    }
}
