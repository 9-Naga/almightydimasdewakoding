package com.example.projectbinar.repository;

import com.example.projectbinar.entity.LoanApplication;
import com.example.projectbinar.entity.LoanReview;
import com.example.projectbinar.entity.User;
import com.example.projectbinar.enums.ReviewStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanReviewRepository extends JpaRepository<LoanReview, Long> {
  Optional<LoanReview> findByLoanApplication(LoanApplication loanApplication);

  Optional<LoanReview> findByLoanApplicationId(Long loanApplicationId);

  List<LoanReview> findByReviewer(User reviewer);

  List<LoanReview> findByReviewStatus(ReviewStatus reviewStatus);

  boolean existsByLoanApplication(LoanApplication loanApplication);
}
