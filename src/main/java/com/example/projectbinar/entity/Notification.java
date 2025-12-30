package com.example.projectbinar.entity;

import com.example.projectbinar.enums.NotificationChannel;
import com.example.projectbinar.enums.NotificationType;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "notification")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "passwordHash", "roles"})
    private User user;

    @ManyToOne
    @JoinColumn(name = "loan_application_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private LoanApplication loanApplication;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private NotificationChannel channel;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.isRead == null) {
            this.isRead = false;
        }
    }
}
