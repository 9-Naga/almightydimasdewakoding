package com.example.projectbinar.entity;

import com.example.projectbinar.enums.ReviewStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

@Entity
@Table(name = "loan_review")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanReview implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "loan_application_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private LoanApplication loanApplication;

    @ManyToOne
    @JoinColumn(name = "reviewer_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "passwordHash", "roles"})
    private User reviewer;

    @Column(name = "review_note", columnDefinition = "TEXT")
    private String reviewNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_status", length = 20)
    private ReviewStatus reviewStatus;

    @Column(name = "created_at")
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
    }
}
