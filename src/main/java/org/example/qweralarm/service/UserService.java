package org.example.qweralarm.service;

import lombok.RequiredArgsConstructor;
import org.example.qweralarm.entity.User;
import org.example.qweralarm.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User findByNickname(String nickname) {
        return userRepository.findByNickname(nickname)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다"));
    }
}
