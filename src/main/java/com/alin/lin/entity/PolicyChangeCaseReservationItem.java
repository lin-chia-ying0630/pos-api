package com.alin.lin.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyChangeCaseReservationItem {
    // 保全變更案號
    private String changeCaseNo;

    // 保全變更項目
    private String changeItemCode;
}
