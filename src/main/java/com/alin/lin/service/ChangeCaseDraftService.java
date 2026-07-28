package com.alin.lin.service;

import com.alin.lin.dto.CreateChangeCaseDto;
import com.alin.lin.dto.CreateChangeCaseRequest;
import com.alin.lin.dto.ChangeCaseEligibilityDto;

public interface ChangeCaseDraftService {
    ChangeCaseEligibilityDto checkEligibility(String policyNo, Integer policySeq, String changeItemCode);

    CreateChangeCaseDto createChangeCase(CreateChangeCaseRequest request);
}
