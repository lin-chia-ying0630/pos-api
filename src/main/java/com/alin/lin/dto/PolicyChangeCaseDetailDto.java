package com.alin.lin.dto;

import com.alin.lin.entity.PolicyChangeField;
import com.alin.lin.entity.PolicyChangeFile;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyChangeCaseDetailDto {
    // 保全受理主資料與變更項目摘要。
    private PolicyChangeCaseDto changeCase;

    // 待覆核的逐欄位異動前後值。
    private List<PolicyChangeField> changedFieldNames;

    // 待覆核的資料列快照異動前後值。
    private List<PolicyChangeFile> changedRecordTypes;
}
