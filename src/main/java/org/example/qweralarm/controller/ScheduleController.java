package org.example.qweralarm.controller;

import lombok.RequiredArgsConstructor;
import org.example.qweralarm.dto.ScheduleRequestDto;
import org.example.qweralarm.service.ScheduleService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService scheduleService;

    @GetMapping("/schedule")
    public String ScheduleP(@AuthenticationPrincipal UserDetails userDetails){
        if(userDetails == null) return "redirect:/auth/login";
        return "schedule";
    }

    @GetMapping("/schedule/list")
    @ResponseBody
    public List<ScheduleRequestDto> list(Principal principal) {
        // 유저의 일정을 [{date: "2024-05-15", content: "연습"}] 형태로 리턴해줘야 함!
        return scheduleService.findAllByUser(principal.getName());
    }

    @PostMapping("/schedule/save")
    @ResponseBody
    public String saveSchedule(@RequestBody ScheduleRequestDto dto, Principal principal){
        String loginId = principal.getName();
        scheduleService.save(dto, loginId);
        return "ok";
    }

    @DeleteMapping("/schedule/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteSchedule(@PathVariable("id") Long id) {
        scheduleService.delete(id); // 서비스의 삭제 로직 호출
        return ResponseEntity.ok().build(); // 성공 시 200 OK 응답
    }

    @PostMapping("/schedule/complete/{id}")
    @ResponseBody
    public ResponseEntity<?> completeSchedule(@PathVariable Long id, Principal principal){
        try{
            boolean currentStatus = scheduleService.toggleScheduleComplete(id, principal.getName());

            return ResponseEntity.ok(Map.of(
                    "Success", true,
                    "isCompleted", currentStatus
            ));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}
