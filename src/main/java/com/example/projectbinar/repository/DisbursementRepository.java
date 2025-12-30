package com.example.projectbinar.repository;

import com.example.projectbinar.entity.Disbursement;
import com.example.projectbinar.entity.LoanApplication;
import com.example.projectbinar.enums.DisbursementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisbursementRepository extends JpaRepository<Disbursement, Long> {
    Optional<Disbursement> findByLoanApplication(LoanApplication loanApplication);
    Optional<Disbursement> findByLoanApplicationId(Long loanApplicationId);
    List<Disbursement> findByStatus(DisbursementStatus status);
    boolean existsByLoanApplication(LoanApplication loanApplication);
}
