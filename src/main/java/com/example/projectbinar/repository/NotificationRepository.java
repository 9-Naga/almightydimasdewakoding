package com.example.projectbinar.repository;

import com.example.projectbinar.entity.Notification;
import com.example.projectbinar.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
  List<Notification> findByUserOrderByCreatedAtDesc(User user);

  List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

  List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

  long countByUserAndIsReadFalse(User user);
}
