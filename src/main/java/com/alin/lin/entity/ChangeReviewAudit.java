package com.alin.lin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeReviewAudit {
    private Long auditId;
    private String eventId;
    private Long reviewId;
    private String reviewKey;
    private String functionCode;
    private String action;
    private String statusBefore;
    private String statusAfter;
    private String operatorId;
    private String reviewRemark;
    private String contentBefore;
    private String contentAfter;
    private String requestId;
    private String traceId;
    private LocalDateTime occurredAt;
    private LocalDateTime createdAt;
}
