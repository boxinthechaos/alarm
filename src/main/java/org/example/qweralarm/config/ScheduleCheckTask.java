package org.example.qweralarm.config;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.qweralarm.entity.Schedule;
import org.example.qweralarm.repository.ScheduleRepository;
import org.example.qweralarm.service.MailService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ScheduleCheckTask {
    private final ScheduleRepository scheduleRepository;
    private final MailService mailService;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void checkAndSendMail(){
        LocalDateTime now = LocalDateTime.now();
        String currentDate = now.toLocalDate().toString();
        String currentTime = now.format(DateTimeFormatter.ofPattern("HH:mm"));

        List<Schedule> list = scheduleRepository.findByTargetDateAndTargetTime(LocalDate.parse(currentDate), currentTime);

        for(Schedule s : list){
            // 2. ✨ 여기서 직접 if문으로 체크!
            if (!s.isSent()) {
                try {
                    mailService.sendEventMail(s.getUser().getEmail(), s.getContent());
                    s.markSend(); // 보냈다고 표시
                    System.out.println("메일 발송 완료: " + s.getContent());
                } catch (Exception e) {
                    System.err.println("발송 실패: " + e.getMessage());
                }
            }
        }
    }
}
