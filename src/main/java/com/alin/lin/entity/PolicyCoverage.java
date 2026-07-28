package com.alin.lin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyCoverage {
    private String coverageId;

    // 保單號碼
    @NotBlank @Size(max = 20) private String policyNo;

    // 保單序號
    @NotNull @Positive private Integer policySeq;

    // 主附約型態
    @NotBlank @Size(max = 16) private String coverageItemType;

    // 主附約序號
    @NotBlank @Size(max = 10) private String coverageItemSeq;

    // 險種代碼
    @NotBlank @Size(max = 32) private String productCode;

    @NotBlank @Size(max = 32) private String productVersion;

    // 年期
    @NotNull @Positive private Integer coverageTermYears;

    // 保額
    @NotNull @DecimalMin("0") @Digits(integer = 16, fraction = 2) private BigDecimal insuredAmount;

    // 保費
    @NotNull @DecimalMin("0") @Digits(integer = 14, fraction = 4) private BigDecimal premiumAmount;

    @NotBlank @Size(min = 3, max = 3) private String currencyCode;

    // 商品版本及保障期間的壽險契約屬性。
    @Size(max = 200) private String productName;
    @Size(max = 32) private String basePlanProductCode;
    @Size(max = 4) private String paymentFrequencyCode;
    @Positive private Integer premiumPaymentTermYears;
    @Size(max = 8) private String coverageTermType;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;

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
