package com.alin.lin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyChangeCaseDto {
    private String changeCaseId;
    // 保單號碼
    private String policyNo;

    // 保單序號
    private Integer policySeq;

    // 保全變更案號
    private String changeCaseNo;

    // 受理狀態
    private String acceptanceStatus;

    // 受理狀態說明
    private String acceptanceStatusDescription;

    // 建檔經辦帳號
    private String createdBy;

    // 覆核帳號
    private String reviewedBy;

    // 覆核時間
    private LocalDateTime reviewedAt;

    // 建立時間
    private LocalDateTime createdAt;

    // 異動人員帳號
    private String updatedBy;

    // 異動時間
    private LocalDateTime updatedAt;

    // 保全變更項目清單
    private String changeItemCodes;

    // 保全變更項目說明清單
    private String changeItemCodeDescriptions;
}
