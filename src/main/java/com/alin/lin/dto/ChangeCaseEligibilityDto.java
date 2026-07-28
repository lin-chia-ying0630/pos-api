package com.alin.lin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChangeCaseEligibilityDto {
    // 保單號碼
    private String policyNo;

    // 保單序號
    private Integer policySeq;

    // 保全變更項目序號
    private String changeItemCode;

    // 是否可以申請
    private boolean eligible;

    // 最近一筆保全變更案號，無歷史案件時為 null
    private String latestChangeCaseNo;

    // 最近一筆受理狀態，無歷史案件時為 null
    private String latestAcceptanceStatus;

    // 不可申請原因
    private String message;
}
