package com.alin.lin.service.impl;

import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.dto.MainAmountChangeDto;
import com.alin.lin.dto.MainAmountChangeRequest;
import com.alin.lin.dto.RideAmountChangeRequest;
import com.alin.lin.dto.RiderAmountChangeListRequest;
import com.alin.lin.entity.PolicyCoverage;
import com.alin.lin.enums.PolicyRideKey;
import com.alin.lin.enums.RideChangeField;
import com.alin.lin.service.AmountChangeSaveService;
import com.alin.lin.service.CodeDescriptionService;
import com.alin.lin.service.PolicyChangeSupportService;
import com.alin.lin.util.PolicyChangeFieldUtil.FieldChange;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import static com.alin.lin.util.PolicyChangeFieldUtil.addAmountChangeIfDifferent;
import static com.alin.lin.util.PolicyChangeFieldUtil.amountEquals;
import static com.alin.lin.util.PolicyChangeFieldUtil.amountToString;
import static com.alin.lin.util.PolicyChangeFieldUtil.requireNotEmpty;
import static com.alin.lin.util.PolicyChangeFieldUtil.requireNotNull;
import static com.alin.lin.util.PolicyChangeFieldUtil.requireText;

@Service
public class AmountChangeSaveServiceImpl implements AmountChangeSaveService {
    private static final String RIDE_CHANGE_FILE = "policy_coverage";

    private final PolicyChangeDao policyChangeDao;
    private final PolicyChangeSupportService policyChangeSupportService;
    private final CodeDescriptionService codeDescriptionService;
    private final ObjectMapper objectMapper;

    public AmountChangeSaveServiceImpl(
            PolicyChangeDao policyChangeDao,
            PolicyChangeSupportService policyChangeSupportService,
            CodeDescriptionService codeDescriptionService,
            ObjectMapper objectMapper
    ) {
        this.policyChangeDao = policyChangeDao;
        this.policyChangeSupportService = policyChangeSupportService;
        this.codeDescriptionService = codeDescriptionService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional
    public MainAmountChangeDto saveMainAmountChange(String changeCaseNo, MainAmountChangeRequest request) {
        policyChangeSupportService.requirePolicy(request.getPolicyNo(), request.getPolicySeq());
        requireText(changeCaseNo, "changeCaseNo");
        requireNotNull(request.getInsuredAmount(), "insuredAmount");

        String changeItemCode = codeDescriptionService.mainAmountChangeItemCode();
        policyChangeSupportService.validateChangeCaseAccess(
                request.getPolicyNo(), request.getPolicySeq(), changeCaseNo, changeItemCode
        );
        policyChangeDao.deleteChangeFieldsByItem(request.getPolicyNo(), request.getPolicySeq(), changeCaseNo, changeItemCode);
        policyChangeDao.deleteChangeFileByItemAndKey(
                request.getPolicyNo(),
                request.getPolicySeq(),
                changeCaseNo,
                changeItemCode,
                RIDE_CHANGE_FILE,
                PolicyRideKey.MAIN.getCoverageItemSeq()
        );
        PolicyCoverage mainRide = policyChangeSupportService.requireMainRide(request.getPolicyNo(), request.getPolicySeq());
        if (amountEquals(mainRide.getInsuredAmount(), request.getInsuredAmount())) {
            policyChangeSupportService.removeEmptyChangeItemAndAcceptance(
                    request.getPolicyNo(), request.getPolicySeq(), changeCaseNo, changeItemCode
            );
            return result(changeCaseNo, changeItemCode, 0);
        }

        FieldChange fieldChange = new FieldChange(
                RideChangeField.INSURED_AMOUNT.fieldName(PolicyRideKey.MAIN.getCoverageItemSeq()),
                PolicyRideKey.MAIN.getCoverageItemSeq(),
                amountToString(mainRide.getInsuredAmount()),
                amountToString(request.getInsuredAmount())
        );

        policyChangeSupportService.ensureChangeCaseSaved(
                request.getPolicyNo(), request.getPolicySeq(), changeCaseNo, changeItemCode
        );
        policyChangeSupportService.upsertFieldChange(
                request.getPolicyNo(), request.getPolicySeq(), changeCaseNo, changeItemCode, fieldChange
        );
        policyChangeDao.upsertChangeFile(
                com.alin.lin.util.UuidV7.next(),
                request.getPolicyNo(),
                request.getPolicySeq(),
                changeCaseNo,
                changeItemCode,
                RIDE_CHANGE_FILE,
                PolicyRideKey.MAIN.getCoverageItemSeq(),
                toJson(rideSnapshot(mainRide, mainRide.getInsuredAmount())),
                toJson(rideSnapshot(mainRide, request.getInsuredAmount()))
        );
        // 002 只異動主附約檔的主約列，對使用者回傳一筆業務異動。
        return result(changeCaseNo, changeItemCode, 1);
    }

    @Override
    @Transactional
    public MainAmountChangeDto saveRiderAmountChange(
            String changeCaseNo,
            String policyNo,
            Integer policySeq,
            RiderAmountChangeListRequest request
    ) {
        policyChangeSupportService.requirePolicy(policyNo, policySeq);
        requireText(changeCaseNo, "changeCaseNo");
        requireNotEmpty(request.getRides(), "rides");

        List<PolicyCoverage> rides = policyChangeDao.findRides(policyNo, policySeq);
        Map<String, PolicyCoverage> rideMap = new LinkedHashMap<>();
        rides.forEach(ride -> rideMap.put(ride.getCoverageItemSeq(), ride));

        List<FieldChange> fieldChanges = new ArrayList<>();
        Set<String> requestedRideOrders = new HashSet<>();
        for (RideAmountChangeRequest changedRide : request.getRides()) {
            requireText(changedRide.getCoverageItemSeq(), "coverageItemSeq");
            requireNotNull(changedRide.getInsuredAmount(), "ride insuredAmount");
            if (!requestedRideOrders.add(changedRide.getCoverageItemSeq())) {
                throw new IllegalArgumentException("附約序號不可重複: " + changedRide.getCoverageItemSeq());
            }
            PolicyCoverage beforeRide = rideMap.get(changedRide.getCoverageItemSeq());
            if (beforeRide == null) {
                throw new NoSuchElementException("找不到附約: " + changedRide.getCoverageItemSeq());
            }
            if (codeDescriptionService.mainRideTypeCode().equals(beforeRide.getCoverageItemType())) {
                throw new IllegalArgumentException("003 附約保額變更不可修改主約");
            }
            addAmountChangeIfDifferent(
                    fieldChanges,
                    RideChangeField.INSURED_AMOUNT.fieldName(changedRide.getCoverageItemSeq()),
                    changedRide.getCoverageItemSeq(),
                    beforeRide.getInsuredAmount(),
                    changedRide.getInsuredAmount()
            );
        }

        String changeItemCode = codeDescriptionService.riderAmountChangeItemCode();
        policyChangeSupportService.validateChangeCaseAccess(policyNo, policySeq, changeCaseNo, changeItemCode);
        policyChangeDao.deleteChangeFieldsByItem(policyNo, policySeq, changeCaseNo, changeItemCode);
        if (fieldChanges.isEmpty()) {
            policyChangeSupportService.removeEmptyChangeItemAndAcceptance(policyNo, policySeq, changeCaseNo, changeItemCode);
            return result(changeCaseNo, changeItemCode, 0);
        }

        policyChangeSupportService.ensureChangeCaseSaved(policyNo, policySeq, changeCaseNo, changeItemCode);
        fieldChanges.forEach(fieldChange -> policyChangeSupportService.upsertFieldChange(
                policyNo, policySeq, changeCaseNo, changeItemCode, fieldChange
        ));
        return result(changeCaseNo, changeItemCode, fieldChanges.size());
    }

    private MainAmountChangeDto result(String changeCaseNo, String changeItemCode, int changedFieldCount) {
        return MainAmountChangeDto.builder()
                .changeCaseNo(changeCaseNo)
                .changeItemCode(changeItemCode)
                .changedFieldCount(changedFieldCount)
                .build();
    }

    private Map<String, Object> rideSnapshot(PolicyCoverage ride, java.math.BigDecimal insuredAmount) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("policyNo", ride.getPolicyNo());
        snapshot.put("policySeq", ride.getPolicySeq());
        snapshot.put("coverageItemType", ride.getCoverageItemType());
        snapshot.put("coverageItemSeq", ride.getCoverageItemSeq());
        snapshot.put("productCode", ride.getProductCode());
        snapshot.put("coverageTermYears", ride.getCoverageTermYears());
        snapshot.put("insuredAmount", insuredAmount);
        snapshot.put("premiumAmount", ride.getPremiumAmount());
        return snapshot;
    }

    private String toJson(Map<String, Object> snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("主約資料轉換失敗", exception);
        }
    }
}
