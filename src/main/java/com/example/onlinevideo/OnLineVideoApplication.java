package com.example.onlinevideo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class OnLineVideoApplication {

    public static void main(String[] args) {
        SpringApplication.run(OnLineVideoApplication.class, args);
    }

}
