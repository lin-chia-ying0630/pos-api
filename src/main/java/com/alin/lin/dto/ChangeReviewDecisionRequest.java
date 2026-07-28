package com.alin.lin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangeReviewDecisionRequest {
    @NotBlank(message = "status 不可空白")
    @Pattern(regexp = "[SC]", message = "覆核狀態只能是 S 或 C")
    private String status;

    private String reviewRemark;
}
