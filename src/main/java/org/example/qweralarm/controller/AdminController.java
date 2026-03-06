package org.example.qweralarm.controller;

import lombok.RequiredArgsConstructor;
import org.example.qweralarm.entity.Alarm;
import org.example.qweralarm.repository.AlarmRepository;
import org.example.qweralarm.service.AlarmService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AlarmService alarmService; // Repository 대신 Service 주입

    @GetMapping
    public String adminPage(){
        return "admin";
    }

    @GetMapping("/alarms")
    @ResponseBody
    public ResponseEntity<?> getAllAlarms() {
        // Service에 로직 위임 (데이터 가공까지 서비스에서 다 해서 넘겨줌)
        List<Map<String, String>> history = alarmService.getAllAlarmsForAdmin();
        return ResponseEntity.ok(history);
    }
}
