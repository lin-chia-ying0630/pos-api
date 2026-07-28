package com.alin.lin.service;

import com.alin.lin.dto.PolicyMasterMaintenanceRequest;
import com.alin.lin.entity.PolicyContract;

public interface PolicyMasterMaintenanceService {
    PolicyContract create(PolicyMasterMaintenanceRequest request, String username);
    PolicyContract update(PolicyMasterMaintenanceRequest request, String username);
    void delete(String policyNo, Integer policySeq, String username);
}
