package com.alin.lin.service;

import com.alin.lin.entity.PolicyContact;
import com.alin.lin.entity.PolicyCoverage;

public interface PolicyDetailMaintenanceService {
    PolicyContact createAddress(PolicyContact address, String username);
    PolicyContact updateAddress(PolicyContact address, String username);
    void deleteAddress(String policyNo, Integer policySeq, String addressTypeCode, String username);
    PolicyCoverage createRide(PolicyCoverage ride, String username);
    PolicyCoverage updateRide(PolicyCoverage ride, String username);
    void deleteRide(String policyNo, Integer policySeq, String coverageItemSeq, String username);
}
