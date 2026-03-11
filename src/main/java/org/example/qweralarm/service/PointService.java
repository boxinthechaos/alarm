package org.example.qweralarm.service;

import jakarta.transaction.Transactional;
import org.example.qweralarm.entity.User;
import org.example.qweralarm.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class PointService {
    private final UserRepository userRepository;

    public PointService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void addPoint(String nickName, Long amount){
        User user = userRepository.findByNickname(nickName)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다"));
        user.setPoint(user.getPoint() + amount);

    }
}
