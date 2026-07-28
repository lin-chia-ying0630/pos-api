package com.alin.lin.entity;

import lombok.Data;
import java.time.LocalDateTime;

/** 保單電話聯絡點；手機與市話由 phoneTypeCode 區分，一個保單可保存多筆。 */
@Data
public class PolicyPhone {
    private String phoneId;
    private String policyNo;
    private Integer policySeq;
    private String phoneTypeCode;
    private String countryCallingCode;
    private String areaCode;
    private String phoneNumber;
    private String extensionNo;
    private String primaryFlag;
    private String activeFlag;
    private String reviewStatus;
    private String createdBy;
    private LocalDateTime createdAt;
    private String updatedBy;
    private LocalDateTime updatedAt;
    private String reviewedBy;
    private LocalDateTime reviewedAt;
    private Long recordVersion;
}
