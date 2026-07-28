package com.alin.lin.dto;

import com.alin.lin.entity.PolicyContact;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 外部可修改欄位 allowlist；稽核、覆核與版本欄位一律由後端產生。 */
@Data
public class PolicyContactMaintenanceRequest {
    @NotBlank
    @Pattern(regexp = com.alin.lin.util.ValidationPatterns.POLICY_NO, message = "policyNo 格式錯誤")
    private String policyNo;
    @NotNull @Positive private Integer policySeq;
    @NotBlank
    @Pattern(regexp = com.alin.lin.util.ValidationPatterns.ADDRESS_TYPE, message = "addressTypeCode 必須為 1 至 8 碼英數字")
    private String addressTypeCode;
    @Size(max = 6) private String postalCode;
    @Size(max = 300) private String addressText;
    @Size(max = 2) private String countryCode;
    @Size(max = 1) private String primaryFlag;

    public java.util.Map<String, Object> asFieldMap() {
        java.util.Map<String, Object> values = new java.util.HashMap<>();
        values.put("policyNo", policyNo); values.put("policySeq", policySeq);
        values.put("addressTypeCode", addressTypeCode);
        values.put("postalCode", postalCode); values.put("addressText", addressText);
        values.put("countryCode", countryCode); values.put("primaryFlag", primaryFlag);
        return values;
    }

    public PolicyContact toEntity() {
        return PolicyContact.builder()
                .policyNo(policyNo).policySeq(policySeq).addressTypeCode(addressTypeCode)
                .postalCode(postalCode).addressText(addressText)
                .countryCode(countryCode).primaryFlag(primaryFlag)
                .build();
    }
}
