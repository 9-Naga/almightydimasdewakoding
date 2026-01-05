package com.example.projectbinar.service;

import com.example.projectbinar.dto.loan.LoanReviewRequest;
import com.example.projectbinar.dto.loan.LoanReviewResponse;
import com.example.projectbinar.entity.LoanApplication;
import com.example.projectbinar.entity.LoanReview;
import com.example.projectbinar.entity.User;
import com.example.projectbinar.enums.LoanStatus;
import com.example.projectbinar.enums.ReviewStatus;
import com.example.projectbinar.exception.BadRequestException;
import com.example.projectbinar.exception.ResourceNotFoundException;
import com.example.projectbinar.repository.LoanReviewRepository;
import com.example.projectbinar.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanReviewService {

  private final LoanReviewRepository loanReviewRepository;
  private final LoanApplicationService loanApplicationService;
  private final NotificationService notificationService;
  private final UserRepository userRepository;

  public LoanReviewService(
      LoanReviewRepository loanReviewRepository,
      LoanApplicationService loanApplicationService,
      NotificationService notificationService,
      UserRepository userRepository) {
    this.loanReviewRepository = loanReviewRepository;
    this.loanApplicationService = loanApplicationService;
    this.notificationService = notificationService;
    this.userRepository = userRepository;
  }

  @Transactional
  public LoanReviewResponse submitReview(Long loanId, Long reviewerId, LoanReviewRequest request) {
    LoanApplication loan = loanApplicationService.getLoanEntityById(loanId);

    // Validate loan status
    if (loan.getStatus() != LoanStatus.SUBMITTED) {
      throw new BadRequestException("Loan application is not in SUBMITTED status");
    }

    // Check if already reviewed
    if (loanReviewRepository.existsByLoanApplication(loan)) {
      throw new BadRequestException("This loan application has already been reviewed");
    }

    User reviewer =
        userRepository
            .findById(reviewerId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", reviewerId));

    LoanReview review =
        LoanReview.builder()
            .loanApplication(loan)
            .reviewer(reviewer)
            .reviewNote(request.getReviewNote())
            .reviewStatus(request.getReviewStatus())
            .build();

    LoanReview savedReview = loanReviewRepository.save(review);

    // Update loan status based on review
    if (request.getReviewStatus() == ReviewStatus.APPROVED) {
      loanApplicationService.updateLoanStatus(loanId, LoanStatus.IN_REVIEW);
      notificationService.sendLoanInReviewNotification(loan);
    } else if (request.getReviewStatus() == ReviewStatus.REJECTED) {
      loanApplicationService.updateLoanStatus(loanId, LoanStatus.REJECT);
      notificationService.sendLoanRejectedNotification(loan, request.getReviewNote());
    }

    return mapToResponse(savedReview);
  }

  public List<LoanReviewResponse> getReviewsByReviewer(Long reviewerId) {
    User reviewer =
        userRepository
            .findById(reviewerId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", reviewerId));
    return loanReviewRepository.findByReviewer(reviewer).stream()
        .map(this::mapToResponse)
        .collect(Collectors.toList());
  }

  public LoanReviewResponse getReviewByLoanId(Long loanId) {
    LoanReview review =
        loanReviewRepository
            .findByLoanApplicationId(loanId)
            .orElseThrow(
                () -> new ResourceNotFoundException("Review not found for loan: " + loanId));
    return mapToResponse(review);
  }

  private LoanReviewResponse mapToResponse(LoanReview review) {
    return LoanReviewResponse.builder()
        .id(review.getId())
        .loanApplicationId(review.getLoanApplication().getId())
        .reviewerId(review.getReviewer().getId())
        .reviewerName(review.getReviewer().getFullname())
        .reviewNote(review.getReviewNote())
        .reviewStatus(review.getReviewStatus())
        .createdAt(review.getCreatedAt())
        .build();
  }
}
