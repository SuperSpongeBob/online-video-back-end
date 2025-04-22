package com.example.onlinevideo.Service;

import jakarta.annotation.Resource;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Component
public class MailService {
    @Resource
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromMail;

    /**
     * 发送简单的邮箱
     *
     * @param to 收件人
     * @param subject 标题
     * @param content 正文内容
     * @param cc 抄送
     */
    public void sendSimpleMail(String to,String subject,  String content,String... cc) {
        //  创建邮箱对象
        SimpleMailMessage message = new SimpleMailMessage();
        try {
            message.setFrom(String.valueOf(new InternetAddress(fromMail,"雄雄同学还小","UTF-8")));    //  发件人
        }catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }
        message.setTo(to);          //  收件人
        message.setSubject(subject);  //  标题
        message.setText(content);   //  内容

        if (cc.length>0){
            message.setCc(cc);
        }

        //  发送邮件
        javaMailSender.send(message);
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
