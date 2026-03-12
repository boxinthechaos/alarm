package org.example.qweralarm.controller;

import lombok.RequiredArgsConstructor;
import org.example.qweralarm.entity.Avatar;
import org.example.qweralarm.entity.Item;
import org.example.qweralarm.entity.User;
import org.example.qweralarm.repository.AvatarRepository;
import org.example.qweralarm.repository.ItemRepository;
import org.example.qweralarm.repository.UserItemRepository;
import org.example.qweralarm.repository.UserRepository;
import org.example.qweralarm.service.ShopService;
import org.example.qweralarm.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/auth/shop")
@RequiredArgsConstructor
public class ShopController {
    private final ItemRepository itemRepository;
    private final ShopService shopService;
    private final UserRepository userRepository;
    private final UserItemRepository userItemRepository;
    private final UserService userService;
    private final AvatarRepository avatarRepository;

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

    @GetMapping("/inventory")
    public String inventoryPage(Model model, @AuthenticationPrincipal UserDetails userDetails){
        User user = userService.findByNickname(userDetails.getUsername());

        model.addAttribute("userItems", userItemRepository.findByUser(user));

        // 📌 핵심 수정 부분: .orElse(new Avatar())를 붙여서 Optional이 아닌 실제 객체를 넘깁니다.
        // 만약 유저가 아바타가 없다면 빈 아바타 객체를 생성해서 보냅니다.
        Avatar avatar = avatarRepository.findByUser(user).orElse(new Avatar());
        model.addAttribute("avatar", avatar);

        return "inventory";
    }

    @PostMapping("/inventory/equip/{itemId}")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> equipItem(@PathVariable Long itemId,
                                                         @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (userDetails == null) {
                response.put("success", false);
                response.put("message", "로그인이 필요합니다.");
                return ResponseEntity.status(401).body(response);
            }

            User user = userService.findByNickname(userDetails.getUsername());
            // 아바타가 없을 경우를 대비해 처리
            Avatar avatar = avatarRepository.findByUser(user)
                    .orElseGet(() -> {
                        Avatar newAvatar = new Avatar();
                        newAvatar.setUser(user);
                        return avatarRepository.save(newAvatar);
                    });

            Item item = itemRepository.findById(itemId)
                    .orElseThrow(() -> new RuntimeException("아이템을 찾을 수 없습니다."));

            if ("CAP".equals(item.getCategory())) {
                avatar.setCurrentHatPath(item.getImagePath());
                avatar.setEquippedHat(item);
            } else if ("CLOTHES".equals(item.getCategory())) {
                avatar.setCurrentClothesPath(item.getImagePath());
            }

            avatarRepository.save(avatar);

            response.put("success", true);
            response.put("newHatPath", avatar.getCurrentHatPath());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // 인텔리제이 콘솔에 에러 출력
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/inventory/unequip")
    @ResponseBody
    public ResponseEntity<?> unequipItem(@RequestParam String category, @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.findByNickname(userDetails.getUsername());
        Avatar avatar = avatarRepository.findByUser(user).orElseThrow();

        if ("CAP".equals(category)) {
            avatar.setCurrentHatPath(null);
            avatar.setEquippedHat(null);
        } else if ("CLOTHES".equals(category)) {
            avatar.setCurrentClothesPath(null);
        }

        avatarRepository.save(avatar);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
}
