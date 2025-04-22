package com.example.onlinevideo.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component
public class ThreadService {
    @Autowired
    private MailService mailService;
    @Qualifier("taskExecutor")
    @Autowired
    private Executor taskExecutor;

    /**
     * 发送邮箱
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     */
    @Async("taskExecutor")
    public void sendSimpleMail(String to, String subject, String content) {
        mailService.sendSimpleMail(to, subject, content);
    }
}
