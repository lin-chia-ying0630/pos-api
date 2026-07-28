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
public class PolicyChangeItem {
    private String changeItemId;

    // 保單號碼
    private String policyNo;

    // 保單序號
    private Integer policySeq;

    // 保全變更案號
    private String changeCaseNo;

    // 保全變更項目
    private String changeItemCode;

    // 建立時間
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String updatedBy;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
}
