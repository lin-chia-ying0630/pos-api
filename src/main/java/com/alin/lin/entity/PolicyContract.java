package com.alin.lin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyContract {
    private String policyContractId;

    // 保單號碼
    private String policyNo;

    // 保單序號
    private Integer policySeq;

    // 總保費
    private BigDecimal premiumAmount;

    // ISO 4217 幣別；所有保障項目必須與契約幣別一致。
    private String currencyCode;

    // 壽險契約基本資料；未完成舊資料回填前允許 null。
    private String policyStatus;
    private LocalDate contractDate;
    private LocalDate effectiveDate;
    private LocalDate maturityDate;
    private Integer premiumPaymentTermYears;
    private Integer coverageTermYears;
    private String coverageTermType;
    private String paymentFrequencyCode;
    private String productCode;
    private String productVersion;
    private String productName;
    private String basePlanProductCode;
    private String applicationNo;
    private String customerCode;
    private String insuranceAgentCode;

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
