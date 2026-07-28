package com.alin.lin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyChangeFile {
    private String changeSnapshotId;
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

    // 異動檔案名稱
    private String changedRecordType;

    // 目標資料列鍵值，例如地址型態。
    private String changedRecordKey;

    // 異動前檔案快照
    private String contentBefore;

    // 異動後檔案快照
    private String contentAfter;

    // 供畫面逐格顯示的 JSON 欄位中文名稱與異動前後值。
    private List<PolicyChangeSnapshotField> snapshotFields;

    // 建立時間
    private LocalDateTime createdAt;

    // 最後更新時間
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
}
