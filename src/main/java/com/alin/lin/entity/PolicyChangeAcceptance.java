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
public class PolicyChangeAcceptance {
    private String changeCaseId;

    // 保單號碼
    private String policyNo;

    // 保單序號
    private Integer policySeq;

    // 保全變更案號
    private String changeCaseNo;

    // 受理狀態
    private String acceptanceStatus;

    // 建檔經辦帳號
    private String createdBy;

    // 覆核帳號
    private String reviewedBy;

    // 覆核時間
    private LocalDateTime reviewedAt;

    // 建立時間
    private LocalDateTime createdAt;

    // 更新時間
    private LocalDateTime updatedAt;

    // 異動人員帳號
    private String updatedBy;
}
