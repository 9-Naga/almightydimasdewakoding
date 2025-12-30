package com.example.projectbinar.dto.loan;

import com.example.projectbinar.enums.ApprovalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoanApprovalResponse {
    private Long id;
    private Long loanApplicationId;
    private Long approverId;
    private String approverName;
    private ApprovalStatus approvalStatus;
    private String approvalNote;
    private Instant approvalDate;
}
