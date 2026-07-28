package com.alin.lin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideAmountChangeRequest {
    // 主附約序號
    @NotBlank(message = "coverageItemSeq 不可空白")
    @Pattern(regexp = com.alin.lin.util.ValidationPatterns.RIDE_ORDER, message = "coverageItemSeq 必須為 1 至 10 碼數字")
    private String coverageItemSeq;

    // 變更後保額
    @NotNull(message = "insuredAmount 不可空白")
    @DecimalMin(value = "0.00", message = "insuredAmount 不可小於 0")
    @Digits(integer = 16, fraction = 2, message = "insuredAmount 最多 16 位整數及 2 位小數")
    private BigDecimal insuredAmount;
}
