package com.example.projectbinar.repository;

import com.example.projectbinar.entity.LoanApplication;
import com.example.projectbinar.entity.LoanApproval;
import com.example.projectbinar.entity.User;
import com.example.projectbinar.enums.ApprovalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanApprovalRepository extends JpaRepository<LoanApproval, Long> {
    Optional<LoanApproval> findByLoanApplication(LoanApplication loanApplication);
    Optional<LoanApproval> findByLoanApplicationId(Long loanApplicationId);
    List<LoanApproval> findByApprover(User approver);
    List<LoanApproval> findByApprovalStatus(ApprovalStatus approvalStatus);
    boolean existsByLoanApplication(LoanApplication loanApplication);
}
