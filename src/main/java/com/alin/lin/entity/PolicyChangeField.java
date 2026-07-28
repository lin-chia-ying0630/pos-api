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
public class PolicyChangeField {
    private String changeFieldId;
    // 流水識別碼
    private Long id;

    // 保單號碼
    private String policyNo;

    // 保單序號
    private Integer policySeq;

    // 保全變更案號
    private String changeCaseNo;

    // 保全變更項目
    private String changeItemCode;

    // 異動欄位
    private String changedFieldName;

    // 畫面顯示的中文欄位名稱，由 CodeDescription 補入，不對應資料表欄位。
    private String chineseName;

    // 異動目標鍵值
    private String changedRecordKey;

    // 異動前內容
    private String contentBefore;

    // 異動後內容
    private String contentAfter;

    // 建立時間
    private LocalDateTime createdAt;

    // 最後更新時間
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
}
