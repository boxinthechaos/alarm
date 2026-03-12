package org.example.qweralarm.repository;

import org.example.qweralarm.entity.Avatar;
import org.example.qweralarm.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AvatarRepository extends JpaRepository<Avatar, Long> {
    Optional<Avatar> findByUser(User user);
}
