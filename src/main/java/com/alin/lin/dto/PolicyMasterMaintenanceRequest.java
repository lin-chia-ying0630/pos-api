package com.alin.lin.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PolicyMasterMaintenanceRequest {
    @NotBlank
    @Pattern(regexp = com.alin.lin.util.ValidationPatterns.POLICY_NO, message = "policyNo 格式錯誤")
    private String policyNo;
    @NotNull @Positive private Integer policySeq;
    @NotNull @DecimalMin("0") @Digits(integer = 14, fraction = 4) private BigDecimal premiumAmount;
    @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$", message = "currencyCode 必須為 3 碼英文字母")
    private String currencyCode;
    @jakarta.validation.constraints.Size(max = 2) private String policyStatus;
    private LocalDate contractDate;
    private LocalDate effectiveDate;
    private LocalDate maturityDate;
    @Positive private Integer premiumPaymentTermYears;
    @Positive private Integer coverageTermYears;
    @jakarta.validation.constraints.Size(max = 8) private String coverageTermType;
    @jakarta.validation.constraints.Size(max = 4) private String paymentFrequencyCode;
    @jakarta.validation.constraints.Size(max = 32) private String productCode;
    @jakarta.validation.constraints.Size(max = 32) private String productVersion;
    @jakarta.validation.constraints.Size(max = 200) private String productName;
    @jakarta.validation.constraints.Size(max = 32) private String basePlanProductCode;
    @jakarta.validation.constraints.Size(max = 32) private String applicationNo;
    @jakarta.validation.constraints.Size(max = 32) private String customerCode;
    @jakarta.validation.constraints.Size(max = 32) private String insuranceAgentCode;
    private String activeFlag;
    private String reviewStatus;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String originalPolicyNo;
    private Integer originalPolicySeq;

    public java.util.Map<String, Object> asFieldMap() {
        java.util.Map<String, Object> values = new java.util.HashMap<>();
        values.put("policyNo", policyNo); values.put("policySeq", policySeq);
        values.put("premiumAmount", premiumAmount); values.put("currencyCode", currencyCode);
        values.put("policyStatus", policyStatus); values.put("contractDate", contractDate);
        values.put("effectiveDate", effectiveDate); values.put("maturityDate", maturityDate);
        values.put("premiumPaymentTermYears", premiumPaymentTermYears);
        values.put("coverageTermYears", coverageTermYears); values.put("coverageTermType", coverageTermType);
        values.put("paymentFrequencyCode", paymentFrequencyCode); values.put("productCode", productCode);
        values.put("productVersion", productVersion); values.put("productName", productName);
        values.put("basePlanProductCode", basePlanProductCode); values.put("applicationNo", applicationNo);
        values.put("customerCode", customerCode); values.put("insuranceAgentCode", insuranceAgentCode);
        return values;
    }
}
