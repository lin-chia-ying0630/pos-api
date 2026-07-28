package com.alin.lin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyContact {
    // 保單號碼
    @NotBlank @Size(max = 20) private String policyNo;

    // 保單序號
    @NotNull @Positive private Integer policySeq;

    // 地址識別碼；新增時由資料庫產生。
    private String addressId;

    // 地址用途
    @NotBlank @Size(max = 8) private String addressTypeCode;

    @Size(max = 6) private String postalCode;
    @Size(max = 300) private String addressText;
    @Size(max = 2) private String countryCode;
    @Size(max = 1) private String primaryFlag;

    // 建立時間
    private LocalDateTime createdAt;

    // 更新時間
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String activeFlag;
    private String reviewStatus;
    // 正式資料樂觀鎖版本；覆核送出與核准時必須一致。
    private Long recordVersion;
}
