package com.example.onlinevideo.Service;

import jakarta.annotation.Resource;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;


@Component
public class MailService {
    private static final Logger log = LoggerFactory.getLogger(MailService.class);
    @Resource
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromMail;

    private static final int MAX_RETRIES = 3;

    /**
     * 发送简单的邮箱
     *
     * @param to      收件人
     * @param subject 标题
     * @param content 正文内容
     * @param cc      抄送
     */
    public void sendSimpleMail(String to, String subject, String content, String... cc) {
        int retries = 0;
        while (retries < MAX_RETRIES) {
            try {
                //  创建邮箱对象
                SimpleMailMessage message = new SimpleMailMessage();
                message.setFrom(String.valueOf(new InternetAddress(fromMail, "雄雄同学还小", "UTF-8")));    //  发件人

                message.setTo(to);          //  收件人
                message.setSubject(subject);  //  标题
                message.setText(content);   //  内容

                if (cc.length > 0) {
                    message.setCc(cc);
                }

                //  发送邮件
                javaMailSender.send(message);
                break;
            } catch (Exception e) {
                retries++;
                //  指数退避重试策略
                try {
                    Thread.sleep((long) Math.pow(2, retries) * 1000); // 1s, 2s, 4s
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }

                if (retries == MAX_RETRIES) {
                    //  TODO 这里可以采用备用渠道，如短信或APP推送等

                    //  记录日志
                    log.error("Failed to send email after " + MAX_RETRIES + " retries: " + e.getMessage());
                }
            }
        }
    }


    /**
     * 发送HTML邮件
     *
     * @param to      收件人地址
     * @param subject 邮件主题
     * @param content 邮件内容
     * @param cc      抄送地址
     * @throws Exception 邮件发送异常
     */
    public void sendHtmlMail(String to, String subject, String content, String... cc) throws Exception {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setFrom(fromMail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        if (cc.length > 0) {
            helper.setCc(cc);
        }
        javaMailSender.send(message);
    }
}
