package com.example.projectbinar.service;

import com.example.projectbinar.dto.notification.NotificationResponse;
import com.example.projectbinar.entity.LoanApplication;
import com.example.projectbinar.entity.Notification;
import com.example.projectbinar.entity.User;
import com.example.projectbinar.enums.NotificationChannel;
import com.example.projectbinar.enums.NotificationType;
import com.example.projectbinar.exception.ResourceNotFoundException;
import com.example.projectbinar.repository.NotificationRepository;
import com.example.projectbinar.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public void sendLoanSubmittedNotification(LoanApplication loan) {
        createNotification(
                loan.getCustomer().getUser(),
                loan,
                NotificationType.LOAN_SUBMITTED,
                "Your loan application for " + loan.getPlafond().getName() + " has been submitted successfully."
        );
    }

    @Transactional
    public void sendLoanInReviewNotification(LoanApplication loan) {
        createNotification(
                loan.getCustomer().getUser(),
                loan,
                NotificationType.LOAN_IN_REVIEW,
                "Your loan application is now being reviewed by our team."
        );
    }

    @Transactional
    public void sendLoanApprovedNotification(LoanApplication loan) {
        createNotification(
                loan.getCustomer().getUser(),
                loan,
                NotificationType.LOAN_APPROVED,
                "Congratulations! Your loan application for " + loan.getAmount() + " has been approved."
        );
    }

    @Transactional
    public void sendLoanRejectedNotification(LoanApplication loan, String reason) {
        createNotification(
                loan.getCustomer().getUser(),
                loan,
                NotificationType.LOAN_REJECTED,
                "We regret to inform you that your loan application has been rejected. Reason: " + reason
        );
    }

    @Transactional
    public void sendLoanDisbursedNotification(LoanApplication loan) {
        createNotification(
                loan.getCustomer().getUser(),
                loan,
                NotificationType.LOAN_DISBURSED,
                "Your loan of " + loan.getAmount() + " has been disbursed to your bank account."
        );
    }

    private void createNotification(User user, LoanApplication loan, NotificationType type, String message) {
        Notification notification = Notification.builder()
                .user(user)
                .loanApplication(loan)
                .type(type)
                .channel(NotificationChannel.IN_APP)
                .message(message)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    public List<NotificationResponse> getNotificationsByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", "id", notificationId));
        notification.setIsRead(true);
        Notification savedNotification = notificationRepository.save(notification);
        return mapToResponse(savedNotification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unread = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId).stream()
                .filter(n -> !n.getIsRead())
                .collect(Collectors.toList());
        unread.forEach(n -> n.setIsRead(true));
        notificationRepository.saveAll(unread);
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .userId(notification.getUser().getId())
                .loanApplicationId(notification.getLoanApplication() != null ? notification.getLoanApplication().getId() : null)
                .type(notification.getType())
                .channel(notification.getChannel())
                .message(notification.getMessage())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
