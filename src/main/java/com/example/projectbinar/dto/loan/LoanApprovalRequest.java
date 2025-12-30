package com.example.projectbinar.dto.loan;

import com.example.projectbinar.enums.ApprovalStatus;
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
public class LoanApprovalRequest {
    
    @NotNull(message = "Approval status is required")
    private ApprovalStatus approvalStatus;
    
    @NotBlank(message = "Approval note is required")
    private String approvalNote;
}
