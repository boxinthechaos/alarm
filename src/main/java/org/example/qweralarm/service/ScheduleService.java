package org.example.qweralarm.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.qweralarm.dto.ScheduleRequestDto;
import org.example.qweralarm.entity.ActivityLog;
import org.example.qweralarm.entity.ActivityType;
import org.example.qweralarm.entity.Schedule;
import org.example.qweralarm.entity.User;
import org.example.qweralarm.repository.ActivityLogRepository;
import org.example.qweralarm.repository.ScheduleRepository;
import org.example.qweralarm.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class ScheduleService {
    private final ScheduleRepository scheduleRepository;
    private final UserRepository userRepository;
    private final ActivityLogRepository activityLogRepository;

    @Transactional
    public void delete(Long id){
        scheduleRepository.deleteById(id);
    }

    @Transactional
    public void save(ScheduleRequestDto dto, String userNickname){
        User user = userRepository.findByNickname(userNickname)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다"));

        Schedule schedule;


        if(dto.getId() != null){
            schedule = scheduleRepository.findById(dto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("수정할 스케줄이 존재하지 않습니다."));

            schedule.updateSchedule(
                    LocalDate.parse(dto.getDate()),
                    dto.getTime(),
                    dto.getContent(),
                    dto.getColor());
        }
        else {
            Schedule newSchedule = Schedule.builder()
                    .targetDate(LocalDate.parse(dto.getDate()))
                    .targetTime(dto.getTime())
                    .content(dto.getContent())
                    .color(dto.getColor())
                    .user(user)
                    .build();
            scheduleRepository.save(newSchedule);
        }
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
                s.getTargetTime(),
                s.isCompleted(),
                s.getColor()
        )).toList();
    }

    @Transactional
    public boolean toggleScheduleComplete(Long ScheduleId, String nickname){
        Schedule schedule = scheduleRepository.findByIdAndUserNickname(ScheduleId, nickname)
                .orElseThrow(() -> new IllegalArgumentException("해당 스케줄이 없거나 권한이 없습니다"));

        if(!schedule.isCompleted()){
            schedule.successSchedule();
            ActivityLog log = ActivityLog.builder()
                    .type(ActivityType.SCHEDULE)
                    .isSuccess(true)
                    .createdAt(LocalDateTime.now())
                    .user(schedule.getUser())
                    .build();
            activityLogRepository.save(log);
        } else {
            schedule.cancelSuccess();
        }

        return schedule.isCompleted();
    }
}
