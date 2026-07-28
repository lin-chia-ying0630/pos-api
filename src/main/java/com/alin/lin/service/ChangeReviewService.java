package com.alin.lin.service;

import com.alin.lin.entity.ChangeReview;
import com.alin.lin.entity.ChangeReviewAudit;
import com.alin.lin.dto.ChangeReviewPageDto;

import java.util.List;

public interface ChangeReviewService {
    ChangeReviewPageDto findReviews(String functionCode, String key1, String reviewStatus, int page);

    List<ChangeReviewAudit> findAuditTrail(String reviewKey);

    void recordSubmission(ChangeReview review);

    void recordDirectCompletion(ChangeReview review, String operatorId);

    void decide(String reviewKey, String status, String reviewRemark, String reviewedBy);
}
