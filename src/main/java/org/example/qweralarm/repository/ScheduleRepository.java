package org.example.qweralarm.repository;


import org.example.qweralarm.entity.Schedule;
import org.example.qweralarm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ScheduleRepository extends JpaRepository<Schedule, Long> {
    List<Schedule> findByUser(User user);
    List<Schedule> findByTargetDateAndTargetTime(LocalDate targetDate, String targetTime);
    Optional<Schedule> findByIdAndUserNickname(Long id, String nickname);
}
