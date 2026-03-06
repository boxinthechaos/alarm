package org.example.qweralarm.controller;

import lombok.RequiredArgsConstructor;
import org.example.qweralarm.entity.Role;
import org.example.qweralarm.entity.User;
import org.example.qweralarm.repository.UserRepository;
import org.example.qweralarm.service.AuthService;
import org.example.qweralarm.service.EmailVerificationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final EmailVerificationService emailVerificationService;
    private final AuthService authService; // Repository 대신 Service 주입
    private final UserRepository userRepository;

    @GetMapping("/login")
    public String loginPage(){
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(){
        return "register";
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestParam String username, @RequestParam String password, @RequestParam String email){
        try{
            authService.registerUser(username, email, password);
            return ResponseEntity.ok("회원가입 성공");
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/send-verification")
    public ResponseEntity<String> sendVerification(@RequestParam String email) {
        System.out.println("🚨 프론트에서 넘어온 이메일: [" + email + "]");
        if(userRepository.existsByEmail(email)){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("이미 존재하는 이메일 입니다");
        }
        try {
            emailVerificationService.sendVerificationCode(email);
            return ResponseEntity.ok("발송 성공");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("이메일 발송에 실패했습니다.");
        }
    }
    @PostMapping("/verify-code")
    public ResponseEntity<String> verifyCode(@RequestParam String email, @RequestParam String code) {
        boolean isVerified = emailVerificationService.verifyCode(email, code);

        if (isVerified) {
            return ResponseEntity.ok("인증 성공");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("인증번호가 일치하지 않습니다.");
        }
    }
}
