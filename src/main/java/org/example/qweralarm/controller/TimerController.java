package org.example.qweralarm.controller;

import org.example.qweralarm.service.PointService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/auth")
public class TimerController {
    private final PointService pointService;

    public TimerController(PointService pointService) {
        this.pointService = pointService;
    }

    @GetMapping("/timer")
    public String timerPage(@AuthenticationPrincipal UserDetails userDetails){
        if(userDetails == null) return "redirect:/auth/login";
        return "timer";
    }
    @PostMapping("/timer/complete")
    @ResponseBody
    public ResponseEntity<String> completeTimer(@AuthenticationPrincipal UserDetails userDetails) {
        String nickname = userDetails.getUsername();
        pointService.addPoint(nickname, 30L);

        return ResponseEntity.ok("집중 성공! 30포인트가 적립되었습니다.");
    }
}
