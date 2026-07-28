package com.alin.lin.service.impl;

import com.alin.lin.dto.AddressChangeDto;
import com.alin.lin.dto.AddressChangeRequest;
import com.alin.lin.dto.CreateChangeCaseDto;
import com.alin.lin.dto.CreateChangeCaseRequest;
import com.alin.lin.dto.ChangeCaseEligibilityDto;
import com.alin.lin.dto.MainAmountChangeDto;
import com.alin.lin.dto.MainAmountChangeRequest;
import com.alin.lin.dto.PolicyChangeCaseDetailDto;
import com.alin.lin.dto.PolicyChangeCaseDto;
import com.alin.lin.dto.PolicyDetailDto;
import com.alin.lin.dto.PostalCodeAreaDto;
import com.alin.lin.dto.RiderAmountChangeListRequest;
import com.alin.lin.dto.UpdateChangeCaseStatusDto;
import com.alin.lin.dto.UpdateChangeCaseStatusRequest;
import com.alin.lin.service.AddressChangeSaveService;
import com.alin.lin.service.ContactChannelChangeService;
import com.alin.lin.service.AmountChangeSaveService;
import com.alin.lin.service.ChangeCaseDraftService;
import com.alin.lin.service.ChangeCaseReviewService;
import com.alin.lin.service.PolicyChangeService;
import com.alin.lin.service.PolicyQueryService;
import com.alin.lin.exception.ChangeCaseConflictException;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Supplier;

/**
 * Controller 使用的單一入口，實際商業規則委派給各 use-case service。
 */
@Service
public class PolicyChangeServiceImpl implements PolicyChangeService {
    private final PolicyQueryService policyQueryService;
    private final ChangeCaseDraftService changeCaseDraftService;
    private final AddressChangeSaveService addressChangeSaveService;
    private final ContactChannelChangeService contactChannelChangeService;
    private final AmountChangeSaveService amountChangeSaveService;
    private final ChangeCaseReviewService changeCaseReviewService;

    public PolicyChangeServiceImpl(
            PolicyQueryService policyQueryService,
            ChangeCaseDraftService changeCaseDraftService,
            AddressChangeSaveService addressChangeSaveService,
            ContactChannelChangeService contactChannelChangeService,
            AmountChangeSaveService amountChangeSaveService,
            ChangeCaseReviewService changeCaseReviewService
    ) {
        this.policyQueryService = policyQueryService;
        this.changeCaseDraftService = changeCaseDraftService;
        this.addressChangeSaveService = addressChangeSaveService;
        this.contactChannelChangeService = contactChannelChangeService;
        this.amountChangeSaveService = amountChangeSaveService;
        this.changeCaseReviewService = changeCaseReviewService;
    }

    @Override
    public PolicyDetailDto findPolicyDetail(String policyNo, Integer policySeq) {
        return policyQueryService.findPolicyDetail(policyNo, policySeq);
    }

    @Override
    public PostalCodeAreaDto findPostalCodeArea(String postalCode) {
        return policyQueryService.findPostalCodeArea(postalCode);
    }

    @Override
    public ChangeCaseEligibilityDto checkChangeCaseEligibility(String policyNo, Integer policySeq, String changeItemCode) {
        return changeCaseDraftService.checkEligibility(policyNo, policySeq, changeItemCode);
    }

    @Override
    public CreateChangeCaseDto createChangeCase(CreateChangeCaseRequest request) {
        return changeCaseDraftService.createChangeCase(request);
    }

    @Override
    public AddressChangeDto saveAddressChange(String changeCaseNo, AddressChangeRequest request) {
        return translateConcurrentConflict(
                () -> addressChangeSaveService.saveAddressChange(changeCaseNo, request)
        );
    }

    @Override
    public AddressChangeDto saveContactChannelChange(
            String changeCaseNo,
            String channel,
            com.alin.lin.dto.ContactChannelChangeRequest request
    ) {
        return translateConcurrentConflict(
                () -> contactChannelChangeService.save(changeCaseNo, channel, request)
        );
    }

    @Override
    public MainAmountChangeDto saveMainAmountChange(String changeCaseNo, MainAmountChangeRequest request) {
        return translateConcurrentConflict(
                () -> amountChangeSaveService.saveMainAmountChange(changeCaseNo, request)
        );
    }

    @Override
    public MainAmountChangeDto saveRiderAmountChange(
            String changeCaseNo,
            String policyNo,
            Integer policySeq,
            RiderAmountChangeListRequest request
    ) {
        return translateConcurrentConflict(
                () -> amountChangeSaveService.saveRiderAmountChange(changeCaseNo, policyNo, policySeq, request)
        );
    }

    @Override
    public List<PolicyChangeCaseDto> findChangeCases(String policyNo) {
        return policyQueryService.findChangeCases(policyNo);
    }

    @Override
    public PolicyChangeCaseDetailDto findChangeCaseDetail(String policyNo, Integer policySeq, String changeCaseNo) {
        return policyQueryService.findChangeCaseDetail(policyNo, policySeq, changeCaseNo);
    }

    @Override
    public UpdateChangeCaseStatusDto updateChangeCaseStatus(
            String changeCaseNo,
            UpdateChangeCaseStatusRequest request
    ) {
        return changeCaseReviewService.updateChangeCaseStatus(changeCaseNo, request);
    }

    /**
     * 同一業務 Key 的並行請求可能在資料庫鎖競爭時由 MySQL 判定其中一筆回滾。
     * 對 API 使用者而言這與「已有案件先進入處理佇列」相同，因此統一回覆 409，
     * 不將資料庫 deadlock 細節洩漏成 500。
     */
    private <T> T translateConcurrentConflict(Supplier<T> action) {
        try {
            return action.get();
        } catch (TransientDataAccessException exception) {
            throw new ChangeCaseConflictException("相同保單異動正在處理中，請稍後重新查詢");
        }
    }
}
