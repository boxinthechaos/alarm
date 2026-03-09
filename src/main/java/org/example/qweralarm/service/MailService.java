package org.example.qweralarm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender mailSender;

    public void sendEventMail(String toEmail, String content){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("pjsleey123412@gmail.com");
        message.setTo(toEmail);
        message.setSubject("[알람] 예약하신 일정이 곧 시작됩니다!");
        message.setText("내용 : " + content);
        mailSender.send(message);
    }
}
