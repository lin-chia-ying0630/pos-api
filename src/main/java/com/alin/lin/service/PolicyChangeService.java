package com.alin.lin.service;

import com.alin.lin.dto.AddressChangeDto;
import com.alin.lin.dto.AddressChangeRequest;
import com.alin.lin.dto.CreateChangeCaseDto;
import com.alin.lin.dto.CreateChangeCaseRequest;
import com.alin.lin.dto.ChangeCaseEligibilityDto;
import com.alin.lin.dto.ContactChannelChangeRequest;
import com.alin.lin.dto.MainAmountChangeDto;
import com.alin.lin.dto.MainAmountChangeRequest;
import com.alin.lin.dto.PolicyChangeCaseDto;
import com.alin.lin.dto.PolicyChangeCaseDetailDto;
import com.alin.lin.dto.PolicyDetailDto;
import com.alin.lin.dto.PostalCodeAreaDto;
import com.alin.lin.dto.RiderAmountChangeListRequest;
import com.alin.lin.dto.UpdateChangeCaseStatusDto;
import com.alin.lin.dto.UpdateChangeCaseStatusRequest;

import java.util.List;

public interface PolicyChangeService {
    PolicyDetailDto findPolicyDetail(String policyNo, Integer policySeq);

    PostalCodeAreaDto findPostalCodeArea(String postalCode);

    ChangeCaseEligibilityDto checkChangeCaseEligibility(String policyNo, Integer policySeq, String changeItemCode);

    CreateChangeCaseDto createChangeCase(CreateChangeCaseRequest request);

    AddressChangeDto saveAddressChange(String changeCaseNo, AddressChangeRequest request);

    AddressChangeDto saveContactChannelChange(String changeCaseNo, String channel, ContactChannelChangeRequest request);

    MainAmountChangeDto saveMainAmountChange(String changeCaseNo, MainAmountChangeRequest request);

    MainAmountChangeDto saveRiderAmountChange(String changeCaseNo, String policyNo, Integer policySeq, RiderAmountChangeListRequest request);

    List<PolicyChangeCaseDto> findChangeCases(String policyNo);

    PolicyChangeCaseDetailDto findChangeCaseDetail(String policyNo, Integer policySeq, String changeCaseNo);

    UpdateChangeCaseStatusDto updateChangeCaseStatus(String changeCaseNo, UpdateChangeCaseStatusRequest request);
}
