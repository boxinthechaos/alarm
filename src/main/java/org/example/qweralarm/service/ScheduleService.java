package org.example.qweralarm.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.qweralarm.dto.ScheduleRequestDto;
import org.example.qweralarm.entity.Schedule;
import org.example.qweralarm.entity.User;
import org.example.qweralarm.repository.ScheduleRepository;
import org.example.qweralarm.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;

    @Transactional
    public void delete(Long id){
        scheduleRepository.deleteById(id);
    }

    @Transactional
    public void save(ScheduleRequestDto dto, String userNickname){
        User user = userRepository.findByNickname(userNickname)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다"));

        Schedule schedule = Schedule.builder()
                .targetDate(LocalDate.parse(dto.getDate()))
                .targetTime(dto.getTime())
                .content(dto.getContent())
                .user(user)
                .build();

        scheduleRepository.save(schedule);
    }
    public List<ScheduleRequestDto> findAllByUser(String nickname) {
        // 1. 에러 나기 전에 출력부터! (이게 먼저 와야 함)
        System.out.println("★로그인 식별자 확인용★: [" + nickname + "]");

        // 2. 그 다음에 유저 찾기
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new NoSuchElementException("DB에 해당 유저가 없습니다: " + nickname));

        List<Schedule> list = scheduleRepository.findByUser(user);

        return list.stream().map(s -> new ScheduleRequestDto(
                s.getId(),
                s.getTargetDate().toString(),
                s.getContent(),
                s.getTargetTime()
        )).toList();
    }
}
