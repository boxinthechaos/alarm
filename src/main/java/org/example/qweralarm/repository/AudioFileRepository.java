package org.example.qweralarm.repository;

import org.example.qweralarm.entity.Alarm;
import org.example.qweralarm.entity.AudioFile;
import org.example.qweralarm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AudioFileRepository extends JpaRepository<AudioFile, Long> {
}
