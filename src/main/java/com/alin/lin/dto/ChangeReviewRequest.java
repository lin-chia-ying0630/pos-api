package com.alin.lin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeReviewRequest {
    @NotBlank private String operation;
    @NotBlank private String sourceType;
    @NotBlank private String sourceRecordType;
    private Long sourceRecordId;
    @NotBlank private String functionCode;
    @NotBlank private String uniqueKey;
    private String policyNo;
    private Integer policySeq;
    private String changeCaseNo;
    private String contentBefore;
    private String contentAfter;
}
