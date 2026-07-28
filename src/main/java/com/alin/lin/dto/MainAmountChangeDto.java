package com.alin.lin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MainAmountChangeDto {
    // 保全變更案號
    private String changeCaseNo;

    // 保全變更項目
    private String changeItemCode;

    // 異動欄位筆數
    private Integer changedFieldCount;
}
