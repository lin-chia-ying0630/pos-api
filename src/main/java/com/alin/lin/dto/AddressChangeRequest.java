package com.alin.lin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressChangeRequest {
    // 保單號碼
    @NotBlank(message = "policyNo 不可空白")
    @Pattern(regexp = com.alin.lin.util.ValidationPatterns.POLICY_NO, message = "policyNo 格式錯誤")
    private String policyNo;

    // 保單序號
    @NotNull(message = "policySeq 不可空白")
    @Positive(message = "policySeq 必須大於 0")
    private Integer policySeq;

    // 地址型態
    @NotBlank(message = "addressTypeCode 不可空白")
    @Pattern(regexp = com.alin.lin.util.ValidationPatterns.ADDRESS_TYPE, message = "addressTypeCode 必須為 1 至 8 碼英數字")
    private String addressTypeCode;

    // 完整郵遞區號。
    @Pattern(regexp = com.alin.lin.util.ValidationPatterns.POSTAL_CODE, message = "postalCode 必須為 3 或 6 碼數字")
    private String postalCode;

    // 地址
    @Size(max = 300, message = "addressText 最多 300 個字元")
    private String addressText;

}
