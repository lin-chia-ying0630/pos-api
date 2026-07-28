package com.alin.lin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MainAmountChangeRequest {
    // 保單號碼
    @NotBlank(message = "policyNo 不可空白")
    @Pattern(regexp = com.alin.lin.util.ValidationPatterns.POLICY_NO, message = "policyNo 格式錯誤")
    private String policyNo;

    // 保單序號
    @NotNull(message = "policySeq 不可空白")
    @Positive(message = "policySeq 必須大於 0")
    private Integer policySeq;

    // 變更後主約保額
    @NotNull(message = "insuredAmount 不可空白")
    @DecimalMin(value = "0.00", message = "insuredAmount 不可小於 0")
    @Digits(integer = 16, fraction = 2, message = "insuredAmount 最多 16 位整數及 2 位小數")
    private BigDecimal insuredAmount;
}
