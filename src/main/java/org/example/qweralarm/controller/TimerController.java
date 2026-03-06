package org.example.qweralarm.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/auth")
public class TimerController {
    @GetMapping("/timer")
    public String timerPage(@AuthenticationPrincipal UserDetails userDetails){
        if(userDetails == null) return "redirect:/auth/login";
        return "timer";
    }
}
