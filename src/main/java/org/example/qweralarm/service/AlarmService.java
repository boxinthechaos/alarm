package org.example.qweralarm.service;

import lombok.RequiredArgsConstructor;
import org.example.qweralarm.entity.Alarm;
import org.example.qweralarm.entity.AudioFile;
import org.example.qweralarm.entity.User;
import org.example.qweralarm.repository.AlarmRepository;
import org.example.qweralarm.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AlarmService {

    private final AlarmRepository alarmRepository;
    private final UserRepository userRepository;
    private final AudioService audioService; // 다른 서비스를 불러와서 협력 가능!

    @Transactional
    public Long createAlarm(String username, String time, MultipartFile file) throws IOException {
        User user = userRepository.findByNickname(username)
                .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다"));

        AudioFile audioToUse;
        if (file != null && !file.isEmpty()) {
            audioToUse = audioService.saveAudioFile(file); // 오디오 서비스에게 위임
        } else {
            audioToUse = audioService.getDefaultAudio();
        }

        Alarm newAlarm = new Alarm();
        newAlarm.setAlarmTime(LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm")));
        newAlarm.setUser(user);
        newAlarm.setAudioFile(audioToUse);
        alarmRepository.save(newAlarm);

        return audioToUse.getId();
    }

    @Transactional(readOnly = true)
    public List<Map<String, String>> getHistoryList(String username) {
        User user = userRepository.findByNickname(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Alarm> alarms = alarmRepository.findByUserOrderByAlarmTimeDesc(user);

        return alarms.stream().map(alarm ->
                Map.of(
                        "time", alarm.getAlarmTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        "song", alarm.getAudioFile().getFilename(),
                        "audioId", String.valueOf(alarm.getAudioFile().getId()),
                        "alarmId", String.valueOf(alarm.getId())
                )
        ).toList();
    }

    @Transactional
    public void deleteAlarmHistory(String username, Long alarmId) {
        Alarm alarm = alarmRepository.findById(alarmId)
                .orElseThrow(() -> new IllegalArgumentException("알람을 찾을 수 없습니다"));

        if (!alarm.getUser().getNickname().equals(username)) {
            throw new SecurityException("권한이 없습니다"); // 본인 알람인지 체크
        }

        alarmRepository.deleteById(alarmId);
    }

    // 관리자용 전체 조회
    @Transactional(readOnly = true)
    public List<Map<String, String>> getAllAlarmsForAdmin() {
        List<Alarm> alarms = alarmRepository.findAllWithDetails();

        return alarms.stream().map(alarm ->
                Map.of(
                        "user", alarm.getUser().getNickname(),
                        "time", alarm.getAlarmTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                        "song", alarm.getAudioFile().getFilename()
                )
        ).toList();
    }
}