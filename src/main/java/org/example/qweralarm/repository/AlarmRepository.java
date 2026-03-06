package org.example.qweralarm.repository;

import org.example.qweralarm.entity.Alarm;
import org.example.qweralarm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    List<Alarm> findByUserOrderByAlarmTimeDesc(User user);

    @Query("SELECT a FROM Alarm a JOIN FETCH a.user JOIN FETCH a.audioFile ORDER BY a.id DESC ")
    List<Alarm> findAllWithDetails();
}
