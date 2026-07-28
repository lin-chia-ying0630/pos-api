package com.alin.lin.service.impl;

import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.entity.PolicyContact;
import com.alin.lin.entity.PolicyContract;
import com.alin.lin.entity.PolicyCoverage;
import com.alin.lin.entity.PolicyChangeField;
import com.alin.lin.entity.PolicyChangeFile;
import com.alin.lin.enums.PolicyRideKey;
import com.alin.lin.enums.RideChangeField;
import com.alin.lin.exception.ChangeCaseConflictException;
import com.alin.lin.service.ChangeCaseApplyService;
import com.alin.lin.service.CodeDescriptionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.alin.lin.util.PolicyChangeFieldUtil.amountEquals;
import static com.alin.lin.util.PolicyChangeFieldUtil.collectAddressFieldChanges;
import static com.alin.lin.util.PolicyChangeFieldUtil.normalizeBlank;

@Service
public class ChangeCaseApplyServiceImpl implements ChangeCaseApplyService {
    private final PolicyChangeDao policyChangeDao;
    private final CodeDescriptionService codeDescriptionService;
    private final ObjectMapper objectMapper;

    public ChangeCaseApplyServiceImpl(
            PolicyChangeDao policyChangeDao,
            CodeDescriptionService codeDescriptionService,
            ObjectMapper objectMapper
    ) {
        this.policyChangeDao = policyChangeDao;
        this.codeDescriptionService = codeDescriptionService;
        this.objectMapper = objectMapper;
    }

    @Override
    public int applyChangeCase(String policyNo, Integer policySeq, String changeCaseNo) {
        List<String> changeItemCodes = policyChangeDao.findChangeItemsByCaseNo(policyNo, policySeq, changeCaseNo);
        String addressChangeItem = codeDescriptionService.addressChangeItemCode();
        String mainAmountChangeItem = codeDescriptionService.mainAmountChangeItemCode();
        String riderAmountChangeItem = codeDescriptionService.riderAmountChangeItemCode();
        String emailChangeItem = codeDescriptionService.emailChangeItemCode();
        String telephoneChangeItem = codeDescriptionService.telephoneChangeItemCode();
        String mobileChangeItem = codeDescriptionService.mobileChangeItemCode();
        int appliedItemCount = 0;
        for (String changeItemCode : changeItemCodes) {
            boolean premiumTotalShouldRefresh = false;
            if (addressChangeItem.equals(changeItemCode)) {
                applyAddressChanges(policyNo, policySeq, changeCaseNo, changeItemCode);
                appliedItemCount++;
                continue;
            }
            if (mainAmountChangeItem.equals(changeItemCode)) {
                premiumTotalShouldRefresh = applyMainAmountChanges(policyNo, policySeq, changeCaseNo, changeItemCode);
                appliedItemCount++;
                if (premiumTotalShouldRefresh) {
                    refreshMasterTotalPremium(policyNo, policySeq);
                }
                continue;
            }
            if (riderAmountChangeItem.equals(changeItemCode)) {
                premiumTotalShouldRefresh = applyRiderAmountChanges(policyNo, policySeq, changeCaseNo, changeItemCode);
                appliedItemCount++;
                if (premiumTotalShouldRefresh) {
                    refreshMasterTotalPremium(policyNo, policySeq);
                }
                continue;
            }
            if (emailChangeItem.equals(changeItemCode)) {
                applyContactChannelChange(policyNo, policySeq, changeCaseNo, changeItemCode, true);
                appliedItemCount++;
                continue;
            }
            if (telephoneChangeItem.equals(changeItemCode) || mobileChangeItem.equals(changeItemCode)) {
                applyContactChannelChange(policyNo, policySeq, changeCaseNo, changeItemCode, false);
                appliedItemCount++;
                continue;
            }
            throw new IllegalArgumentException("不支援的保全變更項目: " + changeItemCode);
        }
        return appliedItemCount;
    }

    private void applyContactChannelChange(
            String policyNo,
            Integer policySeq,
            String changeCaseNo,
            String changeItemCode,
            boolean email
    ) {
        List<PolicyChangeField> fields =
                policyChangeDao.findChangeFieldsByItem(policyNo, policySeq, changeCaseNo, changeItemCode);
        if (fields.size() != 1) {
            throw new IllegalStateException("聯絡方式變更欄位必須恰好一筆: " + changeItemCode);
        }
        PolicyChangeField field = fields.get(0);
        int updated;
        if (field.getContentBefore() == null) {
            // 沒有異動前值代表新增聯絡資料；正式資料只在覆核通過時建立。
            updated = email
                    ? policyChangeDao.insertEmailValue(
                            field.getChangedRecordKey(), policyNo, policySeq, field.getContentAfter())
                    : policyChangeDao.insertPhoneValue(
                            field.getChangedRecordKey(), policyNo, policySeq,
                            changeItemCode.equals(codeDescriptionService.telephoneChangeItemCode()) ? "11" : "12",
                            field.getContentAfter());
        } else {
            updated = email
                    ? policyChangeDao.updateEmailValue(
                            field.getChangedRecordKey(), field.getContentBefore(), field.getContentAfter())
                    : policyChangeDao.updatePhoneValue(
                            field.getChangedRecordKey(), field.getContentBefore(), field.getContentAfter());
        }
        requireSingleRowUpdate(updated, "聯絡方式資料已被其他案件修改，請重新建立變更");
    }

    private void applyAddressChanges(String policyNo, Integer policySeq, String changeCaseNo, String changeItemCode) {
        List<PolicyChangeFile> changedRecordTypes = policyChangeDao.findChangeFilesByItem(policyNo, policySeq, changeCaseNo, changeItemCode);
        if (changedRecordTypes.isEmpty()) {
            throw new IllegalStateException("找不到地址變更檔案: " + changeCaseNo);
        }
        for (PolicyChangeFile changedRecordType : changedRecordTypes) {
            PolicyContact beforeAddress = readAddress(
                    changedRecordType.getContentBefore(), changedRecordType.getChangedRecordKey());
            PolicyContact address = readAddress(
                    changedRecordType.getContentAfter(), changedRecordType.getChangedRecordKey());
            if (address.getAddressTypeCode() == null) {
                throw new IllegalStateException("地址變更快照缺少地址類型，請重新建立變更案件");
            }
            PolicyContact currentAddress = policyChangeDao.findAddressForUpdate(
                    policyNo, policySeq, address.getAddressTypeCode()
            );
            if (currentAddress == null || !collectAddressFieldChanges(currentAddress, beforeAddress).isEmpty()) {
                throw new ChangeCaseConflictException("地址資料已被其他案件修改，請重新建立變更: " + address.getAddressTypeCode());
            }
            requireSingleRowUpdate(policyChangeDao.updateAddress(address), "地址回寫失敗: " + address.getAddressTypeCode());
        }
    }

    private boolean applyMainAmountChanges(String policyNo, Integer policySeq, String changeCaseNo, String changeItemCode) {
        List<PolicyChangeField> changedFieldNames = policyChangeDao.findChangeFieldsByItem(policyNo, policySeq, changeCaseNo, changeItemCode);
        if (changedFieldNames.isEmpty()) {
            throw new IllegalStateException("找不到主保額變更欄位: " + changeCaseNo);
        }
        lockMaster(policyNo, policySeq);
        Map<String, PolicyCoverage> rides = lockedRideMap(policyNo, policySeq);
        for (PolicyChangeField changedFieldName : changedFieldNames) {
            assertRideBeforeValue(rides, changedFieldName);
        }
        boolean premiumChanged = false;
        for (PolicyChangeField changedFieldName : changedFieldNames) {
            if (isRideInsuredAmountField(changedFieldName.getChangedFieldName())) {
                String coverageItemSeq = resolveRideOrder(changedFieldName);
                if (!PolicyRideKey.MAIN.getCoverageItemSeq().equals(coverageItemSeq)) {
                    throw new IllegalArgumentException("002 主約保額變更不可回寫附約: " + coverageItemSeq);
                }
                requireSingleRowUpdate(
                        policyChangeDao.updateRideAmount(policyNo, policySeq, coverageItemSeq, changedFieldName.getContentAfter()),
                        "主約保額回寫失敗: " + coverageItemSeq
                );
                continue;
            }
            if (isRidePremiumField(changedFieldName.getChangedFieldName())) {
                String coverageItemSeq = resolveRideOrder(changedFieldName, RideChangeField.PREMIUM);
                if (!PolicyRideKey.MAIN.getCoverageItemSeq().equals(coverageItemSeq)) {
                    throw new IllegalArgumentException("002 主約變更不可回寫附約保費: " + coverageItemSeq);
                }
                requireSingleRowUpdate(
                        policyChangeDao.updateRidePremium(policyNo, policySeq, coverageItemSeq, changedFieldName.getContentAfter()),
                        "主約保費回寫失敗: " + coverageItemSeq
                );
                premiumChanged = true;
                continue;
            }
            throw new IllegalArgumentException("不支援回寫主約保額欄位: " + changedFieldName.getChangedFieldName());
        }
        return premiumChanged;
    }

    private boolean applyRiderAmountChanges(String policyNo, Integer policySeq, String changeCaseNo, String changeItemCode) {
        List<PolicyChangeField> changedFieldNames = policyChangeDao.findChangeFieldsByItem(policyNo, policySeq, changeCaseNo, changeItemCode);
        if (changedFieldNames.isEmpty()) {
            throw new IllegalStateException("找不到附約保額變更欄位: " + changeCaseNo);
        }
        lockMaster(policyNo, policySeq);
        Map<String, PolicyCoverage> rides = lockedRideMap(policyNo, policySeq);
        for (PolicyChangeField changedFieldName : changedFieldNames) {
            assertRideBeforeValue(rides, changedFieldName);
        }
        boolean premiumChanged = false;
        for (PolicyChangeField changedFieldName : changedFieldNames) {
            if (isRideInsuredAmountField(changedFieldName.getChangedFieldName())) {
                String coverageItemSeq = resolveRideOrder(changedFieldName);
                if (PolicyRideKey.MAIN.getCoverageItemSeq().equals(coverageItemSeq)) {
                    throw new IllegalArgumentException("003 附約保額變更不可回寫主約");
                }
                requireSingleRowUpdate(
                        policyChangeDao.updateRideAmount(policyNo, policySeq, coverageItemSeq, changedFieldName.getContentAfter()),
                        "附約保額回寫失敗: " + coverageItemSeq
                );
                continue;
            }
            if (isRidePremiumField(changedFieldName.getChangedFieldName())) {
                String coverageItemSeq = resolveRideOrder(changedFieldName, RideChangeField.PREMIUM);
                if (PolicyRideKey.MAIN.getCoverageItemSeq().equals(coverageItemSeq)) {
                    throw new IllegalArgumentException("003 附約變更不可回寫主約保費");
                }
                requireSingleRowUpdate(
                        policyChangeDao.updateRidePremium(policyNo, policySeq, coverageItemSeq, changedFieldName.getContentAfter()),
                        "附約保費回寫失敗: " + coverageItemSeq
                );
                premiumChanged = true;
                continue;
            }
            throw new IllegalArgumentException("不支援回寫附約保額欄位: " + changedFieldName.getChangedFieldName());
        }
        return premiumChanged;
    }

    private String resolveRideOrder(PolicyChangeField changedFieldName) {
        return resolveRideOrder(changedFieldName, RideChangeField.INSURED_AMOUNT);
    }

    private String resolveRideOrder(PolicyChangeField changedFieldName, RideChangeField rideChangeField) {
        if (changedFieldName.getChangedRecordKey() != null && !changedFieldName.getChangedRecordKey().isBlank()) {
            return changedFieldName.getChangedRecordKey();
        }
        return rideChangeField.resolveRideOrder(changedFieldName.getChangedFieldName());
    }

    private boolean isRideInsuredAmountField(String fieldName) {
        return RideChangeField.INSURED_AMOUNT.matches(fieldName);
    }

    private boolean isRidePremiumField(String fieldName) {
        return RideChangeField.PREMIUM.matches(fieldName);
    }

    private PolicyContract lockMaster(String policyNo, Integer policySeq) {
        PolicyContract master = policyChangeDao.findMasterForUpdate(policyNo, policySeq);
        if (master == null) {
            throw new ChangeCaseConflictException("保單主檔已不存在，請重新查詢");
        }
        return master;
    }

    private Map<String, PolicyCoverage> lockedRideMap(String policyNo, Integer policySeq) {
        return policyChangeDao.findRidesForUpdate(policyNo, policySeq).stream()
                .collect(Collectors.toMap(PolicyCoverage::getCoverageItemSeq, Function.identity()));
    }

    private void refreshMasterTotalPremium(String policyNo, Integer policySeq) {
        requireSingleRowUpdate(
                policyChangeDao.updateMasterTotalPremiumFromRides(policyNo, policySeq),
                "保單總保費回寫失敗"
        );
    }

    private void requireSingleRowUpdate(int updatedRows, String errorMessage) {
        if (updatedRows != 1) {
            throw new ChangeCaseConflictException(errorMessage);
        }
    }

    private void assertRideBeforeValue(Map<String, PolicyCoverage> rides, PolicyChangeField changedFieldName) {
        String coverageItemSeq;
        if (isRideInsuredAmountField(changedFieldName.getChangedFieldName())) {
            coverageItemSeq = resolveRideOrder(changedFieldName);
            PolicyCoverage ride = requireRide(rides, coverageItemSeq);
            assertAmountUnchanged(ride.getInsuredAmount(), changedFieldName, "主附約保額 " + coverageItemSeq);
            return;
        }
        if (isRidePremiumField(changedFieldName.getChangedFieldName())) {
            coverageItemSeq = resolveRideOrder(changedFieldName, RideChangeField.PREMIUM);
            PolicyCoverage ride = requireRide(rides, coverageItemSeq);
            assertAmountUnchanged(ride.getPremiumAmount(), changedFieldName, "主附約保費 " + coverageItemSeq);
            return;
        }
        throw new IllegalArgumentException("不支援的主附約異動欄位: " + changedFieldName.getChangedFieldName());
    }

    private PolicyCoverage requireRide(Map<String, PolicyCoverage> rides, String coverageItemSeq) {
        PolicyCoverage ride = rides.get(coverageItemSeq);
        if (ride == null) {
            throw new ChangeCaseConflictException("主附約資料已不存在: " + coverageItemSeq);
        }
        return ride;
    }

    private void assertAmountUnchanged(
            java.math.BigDecimal currentValue,
            PolicyChangeField changedFieldName,
            String displayName
    ) {
        java.math.BigDecimal beforeValue = changedFieldName.getContentBefore() == null
                ? null
                : new java.math.BigDecimal(changedFieldName.getContentBefore());
        if (!amountEquals(currentValue, beforeValue)) {
            throw new ChangeCaseConflictException(displayName + " 已被其他案件修改，請重新建立變更");
        }
    }

    private PolicyContact readAddress(String contentAfter, String changedRecordKey) {
        try {
            PolicyContact address = objectMapper.readValue(contentAfter, PolicyContact.class);
            // 地址異動快照只保存正式欄位；changedRecordKey 保存地址用途業務鍵。
            if (address.getAddressTypeCode() == null || address.getAddressTypeCode().isBlank()) {
                address.setAddressTypeCode(normalizeBlank(changedRecordKey));
            }
            address.setPostalCode(normalizeBlank(address.getPostalCode()));
            address.setAddressText(normalizeBlank(address.getAddressText()));
            return address;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("地址變更內容轉換失敗", e);
        }
    }
}
