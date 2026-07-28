package com.alin.lin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ContactChannelChangeRequest {
    @NotBlank
    @Pattern(regexp = com.alin.lin.util.ValidationPatterns.POLICY_NO)
    private String policyNo;

    @NotNull
    @Positive
    private Integer policySeq;

    @Size(max = 36)
    private String contactId;

    @NotBlank
    @Size(max = 254)
    private String value;
}
