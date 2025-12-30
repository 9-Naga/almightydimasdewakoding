package com.example.projectbinar.dto.loan;

import com.example.projectbinar.enums.ReviewStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanReviewResponse {
    private Long id;
    private Long loanApplicationId;
    private Long reviewerId;
    private String reviewerName;
    private String reviewNote;
    private ReviewStatus reviewStatus;
    private Instant createdAt;
}
