package com.example.projectbinar.repository;

import com.example.projectbinar.entity.CustomerProfile;
import com.example.projectbinar.entity.LoanApplication;
import com.example.projectbinar.enums.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByCustomer(CustomerProfile customer);
    List<LoanApplication> findByCustomerId(Long customerId);
    List<LoanApplication> findByStatus(LoanStatus status);
    List<LoanApplication> findByStatusIn(List<LoanStatus> statuses);
    List<LoanApplication> findByCustomerAndStatus(CustomerProfile customer, LoanStatus status);
}
