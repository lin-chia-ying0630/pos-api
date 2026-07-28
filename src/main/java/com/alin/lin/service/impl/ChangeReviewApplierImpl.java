package com.alin.lin.service.impl;

import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.entity.ChangeReview;
import com.alin.lin.entity.CodeDescription;
import com.alin.lin.entity.PolicyContact;
import com.alin.lin.entity.PolicyContract;
import com.alin.lin.entity.PolicyCoverage;
import com.alin.lin.exception.ChangeCaseConflictException;
import com.alin.lin.service.ChangeReviewApplier;
import com.alin.lin.service.validation.InsuranceBusinessValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import com.alin.lin.enums.ReviewSourceType;
import com.alin.lin.util.UuidV7;

import java.time.LocalDateTime;

/** 將已核准的 staging 快照套用至正式表；呼叫端必須位於同一資料庫交易。 */
@Component
public class ChangeReviewApplierImpl implements ChangeReviewApplier {
    private final PolicyChangeDao dao;
    private final ObjectMapper objectMapper;
    private final InsuranceBusinessValidator validator;

    public ChangeReviewApplierImpl(PolicyChangeDao dao, ObjectMapper objectMapper, InsuranceBusinessValidator validator) {
        this.dao = dao;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @Override
    public void apply(ChangeReview review, String operatorId) {
        ReviewSourceType sourceType = ReviewSourceType.canonical(review.getSourceType());
        if (sourceType == null) {
            throw new IllegalArgumentException("不支援的覆核來源類型: " + review.getSourceType());
        }
        switch (sourceType) {
            case POLICY_CONTRACT -> applyContract(review, operatorId);
            case POLICY_CONTACT -> applyContact(review, operatorId);
            case POLICY_COVERAGE -> applyCoverage(review, operatorId);
            case CODE_DEFINITION -> applyCode(review, operatorId);
            default -> throw new IllegalArgumentException("不支援的覆核來源類型: " + review.getSourceType());
        }
    }

    private void applyContract(ChangeReview review, String operatorId) {
        PolicyContract before = read(review.getContentBefore(), PolicyContract.class);
        PolicyContract after = read(review.getContentAfter(), PolicyContract.class);
        switch (review.getOperation()) {
            case "CREATE" -> {
                validator.validateContract(after);
                prepareCreated(after, operatorId);
                if (dao.findMasterForUpdate(after.getPolicyNo(), after.getPolicySeq()) != null
                        || dao.insertPolicyMaster(after) != 1) conflict();
            }
            case "UPDATE" -> {
                validator.validateContract(after);
                requireVersion(before);
                if (dao.applyPolicyContractUpdate(after, before.getRecordVersion(), operatorId) != 1) conflict();
            }
            case "DELETE" -> {
                requireVersion(before);
                if (dao.applyPolicyContractDelete(before.getPolicyNo(), before.getPolicySeq(),
                        before.getRecordVersion(), operatorId) != 1) conflict();
            }
            default -> invalidOperation(review);
        }
    }

    private void applyContact(ChangeReview review, String operatorId) {
        PolicyContact before = read(review.getContentBefore(), PolicyContact.class);
        PolicyContact after = read(review.getContentAfter(), PolicyContact.class);
        switch (review.getOperation()) {
            case "CREATE" -> {
                validator.validateContact(after);
                prepareCreated(after, operatorId);
                if (dao.findAddressForUpdate(after.getPolicyNo(), after.getPolicySeq(), after.getAddressTypeCode()) != null
                        || dao.insertPolicyAddress(after) != 1) conflict();
            }
            case "UPDATE" -> {
                validator.validateContact(after); requireVersion(before);
                if (dao.applyPolicyContactUpdate(after, before.getRecordVersion(), operatorId) != 1) conflict();
            }
            case "DELETE" -> {
                requireVersion(before);
                if (dao.applyPolicyContactDelete(before, before.getRecordVersion(), operatorId) != 1) conflict();
            }
            default -> invalidOperation(review);
        }
    }

    private void applyCoverage(ChangeReview review, String operatorId) {
        PolicyCoverage before = read(review.getContentBefore(), PolicyCoverage.class);
        PolicyCoverage after = read(review.getContentAfter(), PolicyCoverage.class);
        switch (review.getOperation()) {
            case "CREATE" -> {
                validator.validateCoverage(after, true); prepareCreated(after, operatorId);
                if (dao.findRideForUpdate(after.getPolicyNo(), after.getPolicySeq(), after.getCoverageItemSeq()) != null
                        || dao.insertPolicyRide(after) != 1) conflict();
            }
            case "UPDATE" -> {
                validator.validateCoverage(after, false); requireVersion(before);
                if (dao.applyPolicyCoverageUpdate(after, before.getRecordVersion(), operatorId) != 1) conflict();
            }
            case "DELETE" -> {
                requireVersion(before);
                if ("BASE".equalsIgnoreCase(before.getCoverageItemType()) && dao.findRides(
                        before.getPolicyNo(), before.getPolicySeq()).stream()
                        .anyMatch(item -> "RIDER".equalsIgnoreCase(item.getCoverageItemType()))) {
                    throw new IllegalArgumentException("主約仍有有效附約，不可直接刪除");
                }
                if (dao.applyPolicyCoverageDelete(before, before.getRecordVersion(), operatorId) != 1) conflict();
            }
            default -> invalidOperation(review);
        }
    }

    private void applyCode(ChangeReview review, String operatorId) {
        CodeDescription before = read(review.getContentBefore(), CodeDescription.class);
        CodeDescription after = read(review.getContentAfter(), CodeDescription.class);
        switch (review.getOperation()) {
            case "CREATE" -> {
                if (after.getCodeDefinitionId() == null) after.setCodeDefinitionId(UuidV7.next());
                prepareCreated(after, operatorId);
                if (dao.findCode(after.getCodeGroup(), after.getCodeField(), after.getCodeBefore()) != null
                        || dao.insertCode(after) != 1) conflict();
            }
            case "UPDATE" -> {
                requireVersion(before);
                if (dao.applyCodeDefinitionUpdate(before, after, before.getRecordVersion(), operatorId) != 1) conflict();
            }
            case "DELETE" -> {
                requireVersion(before);
                if (dao.applyCodeDefinitionDelete(before, before.getRecordVersion(), operatorId) != 1) conflict();
            }
            default -> invalidOperation(review);
        }
    }

    private <T> T read(String json, Class<T> type) {
        if (json == null) return null;
        try { return objectMapper.readValue(json, type); }
        catch (JsonProcessingException ex) { throw new IllegalStateException("覆核快照格式錯誤", ex); }
    }
    private void requireVersion(Object value) {
        Long version = value instanceof PolicyContract v ? v.getRecordVersion()
                : value instanceof PolicyContact v ? v.getRecordVersion()
                : value instanceof PolicyCoverage v ? v.getRecordVersion()
                : value instanceof CodeDescription v ? v.getRecordVersion() : null;
        if (version == null) throw new ChangeCaseConflictException("覆核快照缺少資料版本，請重新送審");
    }
    private void prepareCreated(Object value, String operatorId) {
        LocalDateTime now = LocalDateTime.now();
        if (value instanceof PolicyContract v) { v.setActiveFlag("Y"); v.setReviewStatus("S"); v.setCreatedBy(operatorId); v.setCreatedAt(now); }
        if (value instanceof PolicyContact v) { v.setActiveFlag("Y"); v.setReviewStatus("S"); v.setCreatedBy(operatorId); v.setCreatedAt(now); }
        if (value instanceof PolicyCoverage v) { v.setActiveFlag("Y"); v.setReviewStatus("S"); v.setCreatedBy(operatorId); v.setCreatedAt(now); }
        if (value instanceof CodeDescription v) { v.setActiveFlag("Y"); v.setReviewStatus("S"); v.setCreatedBy(operatorId); v.setCreatedAt(now); }
    }
    private void conflict() { throw new ChangeCaseConflictException("正式資料版本已改變，請重新查詢後送審"); }
    private void invalidOperation(ChangeReview review) { throw new IllegalArgumentException("不支援的異動操作: " + review.getOperation()); }
}
