package org.example.qweralarm.repository;

import org.example.qweralarm.entity.Item;
import org.example.qweralarm.entity.User;
import org.example.qweralarm.entity.UserItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserItemRepository extends JpaRepository<UserItem, Long> {
    boolean existsByUserAndItem(User user, Item item);
    List<UserItem> findByUser(User user);
    List<UserItem> findByUserAndItem_Category(User user, String category);
}