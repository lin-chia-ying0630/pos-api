package com.alin.lin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CodeDescriptionCreateRequest {
    @NotBlank private String codeGroup;
    @NotBlank private String codeField;
    @NotBlank private String codeBefore;
    private String codeAfter;
    @NotBlank private String codeDescription;
    private String activeFlag;
    private String reviewStatus;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private String originalCodeGroup;
    private String originalCodeField;
    private String originalCodeBefore;
    private String updatedBy;
}
