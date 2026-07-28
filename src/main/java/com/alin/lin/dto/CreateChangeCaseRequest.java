package com.alin.lin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateChangeCaseRequest {
    // 保單號碼
    @NotBlank(message = "policyNo 不可空白")
    @Pattern(regexp = com.alin.lin.util.ValidationPatterns.POLICY_NO, message = "policyNo 格式錯誤")
    private String policyNo;

    // 保單序號
    @NotNull(message = "policySeq 不可空白")
    @Positive(message = "policySeq 必須大於 0")
    private Integer policySeq;

    // 同一案號要辦理的保全變更項目
    @NotEmpty(message = "changeItemCodes 不可空白")
    private List<
            @NotBlank(message = "changeItemCode 不可空白")
            @Pattern(regexp = com.alin.lin.util.ValidationPatterns.CHANGE_ITEM, message = "changeItemCode 格式錯誤")
            String> changeItemCodes;
}
