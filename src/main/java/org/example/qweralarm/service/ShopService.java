package org.example.qweralarm.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.example.qweralarm.entity.Item;
import org.example.qweralarm.entity.User;
import org.example.qweralarm.entity.UserItem;
import org.example.qweralarm.repository.ItemRepository;
import org.example.qweralarm.repository.UserItemRepository;
import org.example.qweralarm.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class ShopService {
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;
    private final UserItemRepository userItemRepository;

    public String buyItem(String nickname, Long itemId){
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 아이디 입니다"));
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않은 아이템 입니다"));

        boolean alreadyOwned = userItemRepository.existsByUserAndItem(user, item);
        if(alreadyOwned){
            return "이미 보유 중인 아이템입니다.";
        }

        if(user.getPoint() < item.getPrice()){
            return "포인트가 부족합니다. (현재 : " + user.getPoint() + "P)";
        }

        user.setPoint(user.getPoint() - item.getPrice());
        UserItem userItem = new UserItem();
        userItem.setUser(user);
        userItem.setItem(item);
        userItem.setPurchaseDate(LocalDateTime.now());
        userItemRepository.save(userItem);

        return "구매 성공!";
    }
}
