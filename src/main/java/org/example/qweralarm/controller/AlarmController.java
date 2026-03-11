package org.example.qweralarm.controller;

import lombok.RequiredArgsConstructor;
import org.example.qweralarm.entity.AudioFile;
import org.example.qweralarm.service.AlarmService;
import org.example.qweralarm.service.AudioService;
import org.example.qweralarm.service.AuthService;
import org.example.qweralarm.service.PointService;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/alarm")
@RequiredArgsConstructor
public class AlarmController {

    private final AlarmService alarmService;
    private final AudioService audioService;
    private final AuthService authService;
    private final PointService pointService;

    @GetMapping
    public String alarmPage(Model model, Principal principal,
        @AuthenticationPrincipal UserDetails userDetails) {
        if(userDetails == null) return "redirect:/auth/login";
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }
        return "alarm";
    }

    @PostMapping("/set")
    @ResponseBody
    public ResponseEntity<?> setAlarm(@RequestParam("time") String time,
                                      @RequestParam(value = "file", required = false) MultipartFile file,
                                      @AuthenticationPrincipal UserDetails userDetails) throws IOException {
        if (userDetails == null) return ResponseEntity.status(401).body("로그인이 필요합니다");

        // Service에 로직 위임
        Long audioId = alarmService.createAlarm(userDetails.getUsername(), time, file);
        return ResponseEntity.ok(Map.of("audioId", audioId));
    }

    @GetMapping("/history")
    @ResponseBody
    public ResponseEntity<?> getHistory(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).body("로그인이 필요합니다.");

        // Service에 로직 위임
        List<Map<String, String>> history = alarmService.getHistoryList(userDetails.getUsername());
        return ResponseEntity.ok(history);
    }

    @PostMapping("/history/delete")
    @ResponseBody
    public ResponseEntity<?> deleteHistory(@RequestParam("alarmId") Long alarmId,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) return ResponseEntity.status(401).body("로그인이 필요합니다");

        try {
            // Service에 로직 위임
            alarmService.deleteAlarmHistory(userDetails.getUsername(), alarmId);
            return ResponseEntity.ok().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/audio/{audioId}")
    public ResponseEntity<UrlResource> streamAudioFile(@PathVariable("audioId") Long audioId) {
        try {
            AudioFile audioFile = audioService.getAudioFileById(audioId);
            UrlResource resource = audioService.getAudioResource(audioFile);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + audioFile.getFilename() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, audioFile.getContent_type())
                    .body(resource);
        } catch (RuntimeException | MalformedURLException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/delete")
    public String deleteUser(@AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        if (userDetails == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "로그인 후 이용해 주세요.");
            return "redirect:/auth/login";
        }

        // Service에 로직 위임
        authService.deleteUser(userDetails.getUsername());
        return "redirect:/auth/logout";
    }

    @PostMapping("/stop")
    @ResponseBody
    public ResponseEntity<?> stopAlarm(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body("로그인이 필요합니다");
        }
        String NickName = userDetails.getUsername();

        pointService.addPoint(NickName, 100L);
        return ResponseEntity.ok("알람 종료! " + NickName + "님 100포인트 적립!");
    }
}