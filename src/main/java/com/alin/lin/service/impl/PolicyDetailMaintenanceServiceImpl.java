package com.alin.lin.service.impl;

import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.entity.ChangeReview;
import com.alin.lin.entity.PolicyContact;
import com.alin.lin.entity.PolicyCoverage;
import com.alin.lin.service.ChangeReviewService;
import com.alin.lin.service.PolicyDetailMaintenanceService;
import com.alin.lin.service.support.PendingReviewGuard;
import com.alin.lin.service.ChangeReviewApplier;
import com.alin.lin.service.validation.InsuranceBusinessValidator;
import com.alin.lin.service.policy.ReviewExecutionPolicy;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;
import com.alin.lin.util.UuidV7;

@Service
public class PolicyDetailMaintenanceServiceImpl implements PolicyDetailMaintenanceService {
    private final PolicyChangeDao dao;
    private final ChangeReviewService reviewService;
    private final ObjectMapper objectMapper;
    private final PendingReviewGuard pendingReviewGuard;
    private final ChangeReviewApplier changeReviewApplier;
    private final InsuranceBusinessValidator businessValidator;
    private final ReviewExecutionPolicy reviewExecutionPolicy;

    public PolicyDetailMaintenanceServiceImpl(PolicyChangeDao dao, ChangeReviewService reviewService,
                                              ObjectMapper objectMapper, PendingReviewGuard pendingReviewGuard,
                                              ChangeReviewApplier changeReviewApplier,
                                              InsuranceBusinessValidator businessValidator,
                                              ReviewExecutionPolicy reviewExecutionPolicy) {
        this.dao = dao;
        this.reviewService = reviewService;
        this.objectMapper = objectMapper;
        this.pendingReviewGuard = pendingReviewGuard;
        this.changeReviewApplier = changeReviewApplier;
        this.businessValidator = businessValidator;
        this.reviewExecutionPolicy = reviewExecutionPolicy;
    }

    @Override @Transactional
    public PolicyContact createAddress(PolicyContact value, String username) {
        requireAddressKey(value);
        value.setAddressId(UuidV7.next());
        businessValidator.validateContact(value);
        pendingReviewGuard.requireNoPending("MPM00002", addressKey(value), "保單地址");
        if (dao.findAddressForUpdate(value.getPolicyNo(), value.getPolicySeq(), value.getAddressTypeCode()) != null)
            throw new IllegalArgumentException("保單地址已存在");
        value.setActiveFlag("Y"); value.setReviewStatus("P"); value.setCreatedBy(username);
        value.setCreatedAt(LocalDateTime.now());
        saveReview("MPM00002", "POLICY_CONTACT", "CONTACT", "CREATE", addressKey(value), value.getPolicyNo(),
                value.getPolicySeq(), null, value, username);
        return value;
    }

    @Override @Transactional
    public PolicyContact updateAddress(PolicyContact value, String username) {
        requireAddressKey(value);
        businessValidator.validateContact(value);
        pendingReviewGuard.requireNoPending("MPM00002", addressKey(value), "保單地址");
        PolicyContact before = dao.findAddress(value.getPolicyNo(), value.getPolicySeq(), value.getAddressTypeCode());
        if (before == null) throw new NoSuchElementException("找不到保單地址");
        defaultAddressAudit(value, before, username);
        value.setReviewStatus("P");
        value.setRecordVersion(before.getRecordVersion());
        PolicyContact after = value;
        saveReview("MPM00002", "POLICY_CONTACT", "CONTACT", "UPDATE", addressKey(value), value.getPolicyNo(),
                value.getPolicySeq(), before, after, username);
        return after;
    }

    @Override @Transactional
    public void deleteAddress(String policyNo, Integer policySeq, String addressTypeCode, String username) {
        pendingReviewGuard.requireNoPending("MPM00002", policyNo + "|" + policySeq + "|" + addressTypeCode, "保單地址");
        PolicyContact before = dao.findAddress(policyNo, policySeq, addressTypeCode);
        if (before == null) throw new NoSuchElementException("找不到保單地址");
        saveReview("MPM00002", "POLICY_CONTACT", "CONTACT", "DELETE", addressKey(before), policyNo, policySeq,
                before, null, username);
    }

    @Override @Transactional
    public PolicyCoverage createRide(PolicyCoverage value, String username) {
        requireRideKey(value);
        value.setCoverageId(UuidV7.next());
        businessValidator.validateCoverage(value, true);
        pendingReviewGuard.requireNoPending("MPM00003", rideKey(value), "保單主附約");
        if (dao.findRideForUpdate(value.getPolicyNo(), value.getPolicySeq(), value.getCoverageItemSeq()) != null)
            throw new IllegalArgumentException("保單主附約已存在");
        value.setActiveFlag("Y"); value.setReviewStatus("P"); value.setCreatedBy(username);
        value.setCreatedAt(LocalDateTime.now());
        saveReview("MPM00003", "POLICY_COVERAGE", "COVERAGE", "CREATE", rideKey(value), value.getPolicyNo(),
                value.getPolicySeq(), null, value, username);
        return value;
    }

    @Override @Transactional
    public PolicyCoverage updateRide(PolicyCoverage value, String username) {
        requireRideKey(value);
        businessValidator.validateCoverage(value, false);
        pendingReviewGuard.requireNoPending("MPM00003", rideKey(value), "保單主附約");
        PolicyCoverage before = dao.findRide(value.getPolicyNo(), value.getPolicySeq(), value.getCoverageItemSeq());
        if (before == null) throw new NoSuchElementException("找不到保單主附約");
        defaultRideAudit(value, before, username);
        value.setReviewStatus("P");
        value.setRecordVersion(before.getRecordVersion());
        PolicyCoverage after = value;
        saveReview("MPM00003", "POLICY_COVERAGE", "COVERAGE", "UPDATE", rideKey(value), value.getPolicyNo(),
                value.getPolicySeq(), before, after, username);
        return after;
    }

    @Override @Transactional
    public void deleteRide(String policyNo, Integer policySeq, String coverageItemSeq, String username) {
        pendingReviewGuard.requireNoPending("MPM00003", policyNo + "|" + policySeq + "|" + coverageItemSeq,
                "保單主附約");
        PolicyCoverage before = dao.findRide(policyNo, policySeq, coverageItemSeq);
        if (before == null) throw new NoSuchElementException("找不到保單主附約");
        saveReview("MPM00003", "POLICY_COVERAGE", "COVERAGE", "DELETE", rideKey(before), policyNo, policySeq,
                before, null, username);
    }

    private void saveReview(String functionCode, String sourceType, String recordType, String operation,
                            String uniqueKey, String policyNo, Integer policySeq, Object before, Object after,
                            String username) {
        String beforeText = json(before);
        String reviewKey = functionCode + "|" + uniqueKey + "|" + UUID.randomUUID();
        ChangeReview review = ChangeReview.builder().reviewUuid(UuidV7.next())
                .operation(operation).workflowMode("STAGED").sourceType(sourceType)
                .sourceRecordType(recordType).sourceRecordId(recordId(before, after))
                .functionCode(functionCode).uniqueKey(uniqueKey)
                .key1(policyNo).reviewKey(reviewKey)
                .policyNo(policyNo).policySeq(policySeq).contentBefore(beforeText).contentAfter(json(after))
                .reviewStatus("P").createdBy(username).createdAt(LocalDateTime.now()).build();
        pendingReviewGuard.acquire(functionCode, uniqueKey, reviewKey, username,
                "MPM00002".equals(functionCode) ? "保單地址" : "保單主附約");
        if (reviewExecutionPolicy.isDirectCompletion(username)) {
            changeReviewApplier.apply(review, username);
            reviewService.recordDirectCompletion(review, username);
        } else {
            if (dao.insertChangeReview(review) != 1) throw new IllegalStateException("新增覆核資料失敗");
            reviewService.recordSubmission(review);
        }
    }

    private String recordId(Object before, Object after) {
        Object value = after == null ? before : after;
        if (value instanceof PolicyContact contact) return contact.getAddressId();
        if (value instanceof PolicyCoverage coverage) return coverage.getCoverageId();
        return null;
    }

    private String json(Object value) {
        if (value == null) return null;
        try { return objectMapper.writeValueAsString(value); }
        catch (JsonProcessingException ex) { throw new IllegalStateException("無法建立覆核快照", ex); }
    }
    private String addressKey(PolicyContact v) {
        return v.getAddressId() == null
                ? v.getPolicyNo()+"|"+v.getPolicySeq()+"|"+v.getAddressTypeCode()
                : v.getAddressId();
    }
    private String rideKey(PolicyCoverage v) { return v.getPolicyNo()+"|"+v.getPolicySeq()+"|"+v.getCoverageItemSeq(); }
    private void requireAddressKey(PolicyContact v) {
        if (v.getPolicyNo()==null || v.getPolicySeq()==null || v.getAddressTypeCode()==null || v.getAddressTypeCode().isBlank())
            throw new IllegalArgumentException("保單號碼、序號與地址類型不可空白");
    }
    private void requireRideKey(PolicyCoverage v) {
        if (v.getPolicyNo()==null || v.getPolicySeq()==null || v.getCoverageItemSeq()==null || v.getCoverageItemSeq().isBlank())
            throw new IllegalArgumentException("保單號碼、序號與附約序號不可空白");
    }
    private void defaultAddressAudit(PolicyContact v, PolicyContact b, String user) {
        v.setAddressId(b.getAddressId());
        v.setActiveFlag(b.getActiveFlag());
        v.setCreatedBy(b.getCreatedBy()); v.setCreatedAt(b.getCreatedAt());
        v.setUpdatedBy(user); v.setUpdatedAt(LocalDateTime.now());
        v.setReviewedBy(null); v.setReviewedAt(null);
    }
    private void defaultRideAudit(PolicyCoverage v, PolicyCoverage b, String user) {
        v.setCoverageId(b.getCoverageId());
        v.setActiveFlag(b.getActiveFlag());
        v.setCreatedBy(b.getCreatedBy()); v.setCreatedAt(b.getCreatedAt());
        v.setUpdatedBy(user); v.setUpdatedAt(LocalDateTime.now());
        v.setReviewedBy(null); v.setReviewedAt(null);
    }
}
