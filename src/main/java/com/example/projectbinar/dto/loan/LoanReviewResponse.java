package com.example.projectbinar.dto.loan;

import com.example.projectbinar.enums.ReviewStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
