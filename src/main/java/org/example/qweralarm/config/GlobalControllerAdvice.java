package org.example.qweralarm.config;

import lombok.RequiredArgsConstructor;
import org.example.qweralarm.entity.Avatar;
import org.example.qweralarm.entity.User;
import org.example.qweralarm.repository.AvatarRepository;
import org.example.qweralarm.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.Optional;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {
    private final UserService userService;
    private final AvatarRepository avatarRepository;

    @ModelAttribute
    public void addAttribute(Model model, @AuthenticationPrincipal UserDetails userDetails){
        if(userDetails!= null){
            User user = userService.findByNickname(userDetails.getUsername());

            Optional<Avatar> avatar = avatarRepository.findByUser(user);

            avatar.ifPresent(value -> model.addAttribute("avatar", value));

            model.addAttribute("userNickname", user.getNickname());
            model.addAttribute("userPoint", user.getPoint());
        }
    }
}
