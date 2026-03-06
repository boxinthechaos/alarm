package org.example.qweralarm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value; // 👈 추가된 부분
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private final JavaMailSender mailSender;
    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    // ⭐ application.properties에 설정한 구글 이메일 아이디를 가져옴 ⭐
    @Value("${spring.mail.username}")
    private String senderEmail;

    public void sendVerificationCode(String toEmail){
        String code = String.format("%06d", new Random().nextInt(1000000));

        SimpleMailMessage message = new SimpleMailMessage();

        // ⭐ 핵심: 보내는 사람 명시! (이게 없어서 구글이 에러 뱉은 거임) ⭐
        message.setFrom(senderEmail);

        message.setTo(toEmail);
        message.setSubject("QWER 알람 회원가입 인증 번호 입니다");
        message.setText("인증 번호는 [" + code + "] 입니다 홈페이지 화면에 입력해주세요");

        mailSender.send(message);
        verificationCodes.put(toEmail, code);
    }

    public boolean verifyCode(String email, String code) {
        String storedCode = verificationCodes.get(email);

        // 메모리에 저장된 코드와 유저가 입력한 코드가 일치하는지 확인
        if (storedCode != null && storedCode.equals(code)) {
            // 인증 성공했으면 메모리에서 삭제 (재사용 방지)
            verificationCodes.remove(email);
            return true;
        }
        return false;
    }
}