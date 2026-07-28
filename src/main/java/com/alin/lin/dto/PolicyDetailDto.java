package com.alin.lin.dto;

import com.alin.lin.entity.CodeDescription;
import com.alin.lin.entity.PolicyContact;
import com.alin.lin.entity.PolicyContract;
import com.alin.lin.entity.PolicyCoverage;
import com.alin.lin.entity.PolicyEmail;
import com.alin.lin.entity.PolicyPhone;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PolicyDetailDto {
    // 保單主檔
    private PolicyContract master;

    // 通訊地址
    private PolicyContact communicationAddress;

    // 保單地址清單
    private List<PolicyContact> addressList;

    private List<PolicyEmail> emailList;

    private List<PolicyPhone> phoneList;

    // 保單主附約清單
    private List<PolicyCoverage> rideList;

    // 地址型態代碼清單
    private List<CodeDescription> addressTypeCodes;

    // 受理狀態代碼清單
    private List<CodeDescription> acceptanceStatuses;

    // 保全變更項目代碼清單
    private List<CodeDescription> changeItemCodes;

    // 畫面支線與角色代碼對照
    private List<CodeDescription> screenPermissions;
}
