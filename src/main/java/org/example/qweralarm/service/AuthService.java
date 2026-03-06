package org.example.qweralarm.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.qweralarm.entity.Role;
import org.example.qweralarm.entity.User;
import org.example.qweralarm.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Transactional
    public boolean registerUser(String username, String email, String password) {
        if (userRepository.existsByNickname(username)) {
            throw new IllegalArgumentException("이미 존재하는 닉네임 입니다");
        }

        if(userRepository.existsByEmail(email)){
            throw new IllegalArgumentException("이미 존재하는 이메일 입니다");
        }

        User user = User.builder()
                .nickname(username)
                .email(email)
                .password(bCryptPasswordEncoder.encode(password))
                .role(Role.ROLE_USER)
                .build();

        userRepository.save(user);
        return true;
    }

    @Transactional
    public void deleteUser(String username) {
        userRepository.findByNickname(username).ifPresent(userRepository::delete);
    }
}