package org.example.qweralarm.controller;

import lombok.RequiredArgsConstructor;
import org.example.qweralarm.entity.User;
import org.example.qweralarm.repository.ItemRepository;
import org.example.qweralarm.repository.UserItemRepository;
import org.example.qweralarm.repository.UserRepository;
import org.example.qweralarm.service.ShopService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/auth/shop")
@RequiredArgsConstructor
public class ShopController {
    private final ItemRepository itemRepository;
    private final ShopService shopService;
    private final UserRepository userRepository;
    private final UserItemRepository userItemRepository;

    @GetMapping
    public String shopPage(Model model, @AuthenticationPrincipal UserDetails userDetails){
        if(userDetails == null) return "redirect:/auth/login";
        model.addAttribute("items", itemRepository.findAll());
        User user = userRepository.findByNickname(userDetails.getUsername()).orElseThrow();
        List<Long> ownedItemIds = userItemRepository.findByUser(user)
                .stream()
                .map(userItem -> userItem.getItem().getId())
                .toList();

        model.addAttribute("ownedItemIds", ownedItemIds);
        return "shop";
    }

    @PostMapping("/buy/{itemId}")
    @ResponseBody
    public ResponseEntity<String> buyItem(@PathVariable Long itemId,
                                          @AuthenticationPrincipal UserDetails userDetails){
        String result = shopService.buyItem(userDetails.getUsername() , itemId);

        if(result.equals("구매 성공!")){
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
}
