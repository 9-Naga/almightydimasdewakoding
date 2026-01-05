package com.example.projectbinar.dto.loan;

import com.example.projectbinar.enums.ReviewStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanReviewRequest {

  @NotNull(message = "Review status is required") private ReviewStatus reviewStatus;

  @NotBlank(message = "Review note is required")
  private String reviewNote;
}
