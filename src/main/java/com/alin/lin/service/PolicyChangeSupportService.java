package com.alin.lin.service;

import com.alin.lin.entity.PolicyContract;
import com.alin.lin.entity.PolicyCoverage;
import com.alin.lin.util.PolicyChangeFieldUtil.FieldChange;

public interface PolicyChangeSupportService {
    PolicyContract requirePolicy(String policyNo, Integer policySeq);

    PolicyCoverage requireMainRide(String policyNo, Integer policySeq);

    void validateChangeCaseAccess(String policyNo, Integer policySeq, String changeCaseNo, String changeItemCode);

    void ensureChangeCaseSaved(String policyNo, Integer policySeq, String changeCaseNo, String changeItemCode);

    void upsertFieldChange(String policyNo, Integer policySeq, String changeCaseNo, String changeItemCode, FieldChange fieldChange);

    void removeEmptyChangeItemAndAcceptance(String policyNo, Integer policySeq, String changeCaseNo, String changeItemCode);
}
