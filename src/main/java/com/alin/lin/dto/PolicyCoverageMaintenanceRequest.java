package com.alin.lin.dto;

import com.alin.lin.entity.PolicyCoverage;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/** 外部可修改欄位 allowlist；稽核、覆核與版本欄位一律由後端產生。 */
@Data
public class PolicyCoverageMaintenanceRequest {
    @NotBlank
    @Pattern(regexp = com.alin.lin.util.ValidationPatterns.POLICY_NO, message = "policyNo 格式錯誤")
    private String policyNo;
    @NotNull @Positive private Integer policySeq;
    @NotBlank @Pattern(regexp = "^(?i:BASE|RIDER)$", message = "coverageItemType 只能是 BASE 或 RIDER")
    private String coverageItemType;
    @NotBlank
    @Pattern(regexp = com.alin.lin.util.ValidationPatterns.RIDE_ORDER, message = "coverageItemSeq 必須為 1 至 10 碼數字")
    private String coverageItemSeq;
    @NotBlank @Size(max = 32) private String productCode;
    @NotBlank @Size(max = 32) private String productVersion;
    @NotNull @Positive private Integer coverageTermYears;
    @NotNull @DecimalMin("0") @Digits(integer = 16, fraction = 2) private BigDecimal insuredAmount;
    @NotNull @DecimalMin("0") @Digits(integer = 14, fraction = 4) private BigDecimal premiumAmount;
    @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$", message = "currencyCode 必須為 3 碼英文字母")
    private String currencyCode;
    @Size(max = 200) private String productName;
    @Size(max = 32) private String basePlanProductCode;
    @Size(max = 4) private String paymentFrequencyCode;
    @Positive private Integer premiumPaymentTermYears;
    @Size(max = 8) private String coverageTermType;
    private LocalDate effectiveDate;
    private LocalDate expiryDate;

    public java.util.Map<String, Object> asFieldMap() {
        java.util.Map<String, Object> values = new java.util.HashMap<>();
        values.put("policyNo", policyNo); values.put("policySeq", policySeq);
        values.put("coverageItemType", coverageItemType); values.put("coverageItemSeq", coverageItemSeq);
        values.put("productCode", productCode); values.put("productVersion", productVersion);
        values.put("coverageTermYears", coverageTermYears); values.put("insuredAmount", insuredAmount);
        values.put("premiumAmount", premiumAmount); values.put("currencyCode", currencyCode);
        values.put("productName", productName); values.put("basePlanProductCode", basePlanProductCode);
        values.put("paymentFrequencyCode", paymentFrequencyCode);
        values.put("premiumPaymentTermYears", premiumPaymentTermYears);
        values.put("coverageTermType", coverageTermType); values.put("effectiveDate", effectiveDate);
        values.put("expiryDate", expiryDate);
        return values;
    }

    public PolicyCoverage toEntity() {
        return PolicyCoverage.builder()
                .policyNo(policyNo).policySeq(policySeq).coverageItemType(coverageItemType)
                .coverageItemSeq(coverageItemSeq).productCode(productCode).productVersion(productVersion)
                .coverageTermYears(coverageTermYears).insuredAmount(insuredAmount)
                .premiumAmount(premiumAmount).currencyCode(currencyCode)
                .productName(productName).basePlanProductCode(basePlanProductCode)
                .paymentFrequencyCode(paymentFrequencyCode).premiumPaymentTermYears(premiumPaymentTermYears)
                .coverageTermType(coverageTermType).effectiveDate(effectiveDate).expiryDate(expiryDate)
                .build();
    }
}
