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
public class ChangeReview {
    private Long id;
    private String reviewUuid;
    private String operation;
    // LEGACY=舊流程已先動正式表、STAGED=核准才套用、DIRECT=Admin 直接完成。
    private String workflowMode;
    private String sourceType;
    private String sourceRecordType;
    private String sourceRecordId;
    private String functionCode;
    // 結構化主要查詢鍵，避免以 LIKE 掃描含個資的 JSON 快照。
    private String key1;
    private String uniqueKey;
    private String reviewKey;
    private String policyNo;
    private Integer policySeq;
    private String changeCaseNo;
    private String contentBefore;
    private String contentAfter;
    private String reviewRemark;
    private String reviewStatus;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
}
