package com.alin.lin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeDescription {
    private String codeDefinitionId;
    // 代碼群組
    private String codeGroup;

    // 代碼欄位
    private String codeField;

    // 轉換前代碼
    private String codeBefore;

    // 轉換後代碼
    private String codeAfter;

    // 代碼中文或英文說明
    private String codeDescription;
    // 是否有效：Y 顯示，N 隱藏
    private String activeFlag;
    private String createdBy;
    private java.time.LocalDateTime createdAt;
    private String updatedBy;
    private java.time.LocalDateTime updatedAt;
    private String reviewStatus;
    private String reviewedBy;
    private java.time.LocalDateTime reviewedAt;
    private Long recordVersion;
}
