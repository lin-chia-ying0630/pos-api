package com.alin.lin.service.impl;

import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.dto.PolicyMasterMaintenanceRequest;
import com.alin.lin.entity.ChangeReview;
import com.alin.lin.entity.PolicyContract;
import com.alin.lin.service.ChangeReviewService;
import com.alin.lin.service.PolicyMasterMaintenanceService;
import com.alin.lin.service.support.PendingReviewGuard;
import com.alin.lin.service.ChangeReviewApplier;
import com.alin.lin.service.policy.ReviewExecutionPolicy;
import com.alin.lin.service.validation.InsuranceBusinessValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;
import com.alin.lin.util.UuidV7;

@Service
public class PolicyMasterMaintenanceServiceImpl implements PolicyMasterMaintenanceService {
    private static final String FUNCTION_CODE = "MPM00001";
    private final PolicyChangeDao policyChangeDao;
    private final ChangeReviewService changeReviewService;
    private final ObjectMapper objectMapper;
    private final PendingReviewGuard pendingReviewGuard;
    private final ChangeReviewApplier changeReviewApplier;
    private final ReviewExecutionPolicy reviewExecutionPolicy;
    private final InsuranceBusinessValidator businessValidator;

    public PolicyMasterMaintenanceServiceImpl(PolicyChangeDao policyChangeDao,
                                              ChangeReviewService changeReviewService,
                                              ObjectMapper objectMapper,
                                              PendingReviewGuard pendingReviewGuard,
                                              ChangeReviewApplier changeReviewApplier,
                                              ReviewExecutionPolicy reviewExecutionPolicy,
                                              InsuranceBusinessValidator businessValidator) {
        this.policyChangeDao = policyChangeDao;
        this.changeReviewService = changeReviewService;
        this.objectMapper = objectMapper;
        this.pendingReviewGuard = pendingReviewGuard;
        this.changeReviewApplier = changeReviewApplier;
        this.reviewExecutionPolicy = reviewExecutionPolicy;
        this.businessValidator = businessValidator;
    }

    @Override
    @Transactional
    public PolicyContract create(PolicyMasterMaintenanceRequest request, String username) {
        String uniqueKey = request.getPolicyNo() + "|" + request.getPolicySeq();
        pendingReviewGuard.requireNoPending(FUNCTION_CODE, uniqueKey, "保單主檔");
        if (policyChangeDao.findMasterForUpdate(request.getPolicyNo(), request.getPolicySeq()) != null) {
            throw new IllegalArgumentException("保單主檔已存在");
        }
        PolicyContract master = PolicyContract.builder()
                .policyContractId(UuidV7.next())
                .policyNo(request.getPolicyNo()).policySeq(request.getPolicySeq()).premiumAmount(request.getPremiumAmount())
                .currencyCode(request.getCurrencyCode().toUpperCase())
                .policyStatus(request.getPolicyStatus()).contractDate(request.getContractDate())
                .effectiveDate(request.getEffectiveDate()).maturityDate(request.getMaturityDate())
                .premiumPaymentTermYears(request.getPremiumPaymentTermYears())
                .coverageTermYears(request.getCoverageTermYears()).coverageTermType(request.getCoverageTermType())
                .paymentFrequencyCode(request.getPaymentFrequencyCode()).productCode(request.getProductCode())
                .productVersion(request.getProductVersion()).productName(request.getProductName())
                .basePlanProductCode(request.getBasePlanProductCode()).applicationNo(request.getApplicationNo())
                .customerCode(request.getCustomerCode()).insuranceAgentCode(request.getInsuranceAgentCode())
                .activeFlag("Y").reviewStatus("P").createdBy(username).createdAt(LocalDateTime.now()).build();
        businessValidator.validateContract(master);
        saveReview("CREATE", master, null, master, username);
        return master;
    }

    @Override
    @Transactional
    public PolicyContract update(PolicyMasterMaintenanceRequest request, String username) {
        String originalNo = request.getOriginalPolicyNo() == null ? request.getPolicyNo() : request.getOriginalPolicyNo();
        Integer originalSeq = request.getOriginalPolicySeq() == null ? request.getPolicySeq() : request.getOriginalPolicySeq();
        if (!request.getPolicyNo().equals(originalNo) || !request.getPolicySeq().equals(originalSeq)) {
            throw new IllegalArgumentException("保單號碼與序號不可修改");
        }
        pendingReviewGuard.requireNoPending(FUNCTION_CODE, originalNo + "|" + originalSeq, "保單主檔");
        PolicyContract before = policyChangeDao.findMaster(originalNo, originalSeq);
        if (before == null) throw new NoSuchElementException("找不到保單主檔");
        request.setOriginalPolicyNo(originalNo);
        request.setOriginalPolicySeq(originalSeq);
        PolicyContract after = PolicyContract.builder()
                .policyContractId(before.getPolicyContractId())
                .policyNo(before.getPolicyNo()).policySeq(before.getPolicySeq())
                .premiumAmount(request.getPremiumAmount())
                .currencyCode(request.getCurrencyCode().toUpperCase())
                .policyStatus(request.getPolicyStatus()).contractDate(request.getContractDate())
                .effectiveDate(request.getEffectiveDate()).maturityDate(request.getMaturityDate())
                .premiumPaymentTermYears(request.getPremiumPaymentTermYears())
                .coverageTermYears(request.getCoverageTermYears()).coverageTermType(request.getCoverageTermType())
                .paymentFrequencyCode(request.getPaymentFrequencyCode()).productCode(request.getProductCode())
                .productVersion(request.getProductVersion()).productName(request.getProductName())
                .basePlanProductCode(request.getBasePlanProductCode()).applicationNo(request.getApplicationNo())
                .customerCode(request.getCustomerCode()).insuranceAgentCode(request.getInsuranceAgentCode())
                .activeFlag(before.getActiveFlag())
                .reviewStatus("P").recordVersion(before.getRecordVersion())
                .createdBy(before.getCreatedBy()).createdAt(before.getCreatedAt())
                .updatedBy(username).updatedAt(LocalDateTime.now()).build();
        businessValidator.validateContract(after);
        saveReview("UPDATE", after, before, after, username);
        return after;
    }

    @Override
    @Transactional
    public void delete(String policyNo, Integer policySeq, String username) {
        pendingReviewGuard.requireNoPending(FUNCTION_CODE, policyNo + "|" + policySeq, "保單主檔");
        PolicyContract before = policyChangeDao.findMaster(policyNo, policySeq);
        if (before == null) throw new NoSuchElementException("找不到保單主檔");
        saveReview("DELETE", before, before, null, username);
    }

    private void saveReview(String operation, PolicyContract source, PolicyContract before,
                            PolicyContract after, String username) {
        String uniqueKey = source.getPolicyNo() + "|" + source.getPolicySeq();
        String beforeText = json(before);
        // reviewKey 是每次提交的技術識別；業務防重只使用 functionCode + uniqueKey。
        String reviewKey = FUNCTION_CODE + "|" + uniqueKey + "|" + UUID.randomUUID();
        ChangeReview review = ChangeReview.builder()
                .reviewUuid(UuidV7.next())
                .operation(operation).workflowMode("STAGED")
                .sourceType("POLICY_CONTRACT").sourceRecordType("CONTRACT")
                .sourceRecordId(source.getPolicyContractId())
                .functionCode(FUNCTION_CODE).uniqueKey(uniqueKey).reviewKey(reviewKey)
                .key1(source.getPolicyNo())
                .policyNo(source.getPolicyNo()).policySeq(source.getPolicySeq())
                .contentBefore(beforeText).contentAfter(json(after)).reviewStatus("P")
                .createdBy(username).createdAt(LocalDateTime.now()).build();
        pendingReviewGuard.acquire(FUNCTION_CODE, uniqueKey, reviewKey, username, "保單主檔");
        if (reviewExecutionPolicy.isDirectCompletion(username)) {
            changeReviewApplier.apply(review, username);
            changeReviewService.recordDirectCompletion(review, username);
        } else {
            if (policyChangeDao.insertChangeReview(review) != 1) throw new IllegalStateException("新增覆核資料失敗");
            changeReviewService.recordSubmission(review);
        }
    }

    private String json(PolicyContract value) {
        if (value == null) return null;
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("無法建立保單主檔覆核快照", exception);
        }
    }
}
