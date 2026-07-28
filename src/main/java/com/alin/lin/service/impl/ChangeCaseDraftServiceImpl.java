package com.alin.lin.service.impl;

import com.alin.lin.config.PosChangeProperties;
import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.dto.CreateChangeCaseDto;
import com.alin.lin.dto.CreateChangeCaseRequest;
import com.alin.lin.dto.ChangeCaseEligibilityDto;
import com.alin.lin.dto.PolicyChangeCaseDto;
import com.alin.lin.exception.ChangeCaseConflictException;
import com.alin.lin.entity.PolicyChangeCaseReservation;
import com.alin.lin.entity.PolicyChangeCaseReservationItem;
import com.alin.lin.service.ChangeCaseDraftService;
import com.alin.lin.service.CodeDescriptionService;
import com.alin.lin.service.CurrentUserService;
import com.alin.lin.service.PolicyChangeSupportService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.alin.lin.util.PolicyChangeFieldUtil.requireText;
import static com.alin.lin.util.PiiMaskingUtil.maskPolicyNo;

@Service
public class ChangeCaseDraftServiceImpl implements ChangeCaseDraftService {
    private static final Logger log = LoggerFactory.getLogger(ChangeCaseDraftServiceImpl.class);
    private static final DateTimeFormatter MONTH_DAY_FORMATTER = DateTimeFormatter.ofPattern("MMdd");
    private static final String PENDING_APPLICATION_MESSAGE = "此保單正在受理中，無法申請";

    private final PolicyChangeDao policyChangeDao;
    private final PolicyChangeSupportService policyChangeSupportService;
    private final CodeDescriptionService codeDescriptionService;
    private final CurrentUserService currentUserService;
    private final ZoneId changeCaseZoneId;
    private final Duration reservationTtl;

    public ChangeCaseDraftServiceImpl(
            PolicyChangeDao policyChangeDao,
            PolicyChangeSupportService policyChangeSupportService,
            CodeDescriptionService codeDescriptionService,
            CurrentUserService currentUserService,
            PosChangeProperties posChangeProperties
    ) {
        this.policyChangeDao = policyChangeDao;
        this.policyChangeSupportService = policyChangeSupportService;
        this.codeDescriptionService = codeDescriptionService;
        this.currentUserService = currentUserService;
        this.changeCaseZoneId = ZoneId.of(posChangeProperties.getZoneId());
        this.reservationTtl = posChangeProperties.getReservationTtl();
    }

    @Override
    public ChangeCaseEligibilityDto checkEligibility(String policyNo, Integer policySeq, String changeItemCode) {
        policyChangeSupportService.requirePolicy(policyNo, policySeq);
        requireText(changeItemCode, "changeItemCode");
        PolicyChangeCaseDto latestCase = policyChangeDao.findLatestChangeCaseByItem(
                policyNo, policySeq, changeItemCode.trim()
        );
        boolean eligible = latestCase == null
                || !codeDescriptionService.pendingStatusCode().equalsIgnoreCase(latestCase.getAcceptanceStatus());
        log.info(
                "保全申請資格檢核完成 policyNo={}, policySeq={}, changeItemCode={}, eligible={}, latestStatus={}",
                maskPolicyNo(policyNo),
                policySeq,
                changeItemCode.trim(),
                eligible,
                latestCase == null ? "NONE" : latestCase.getAcceptanceStatus()
        );
        return ChangeCaseEligibilityDto.builder()
                .policyNo(policyNo)
                .policySeq(policySeq)
                .changeItemCode(changeItemCode.trim())
                .eligible(eligible)
                .latestChangeCaseNo(latestCase == null ? null : latestCase.getChangeCaseNo())
                .latestAcceptanceStatus(latestCase == null ? null : latestCase.getAcceptanceStatus())
                .message(eligible ? "" : PENDING_APPLICATION_MESSAGE)
                .build();
    }

    @Override
    @Transactional
    public CreateChangeCaseDto createChangeCase(CreateChangeCaseRequest request) {
        policyChangeSupportService.requirePolicy(request.getPolicyNo(), request.getPolicySeq());
        List<String> changeItemCodes = normalizeChangeItems(request.getChangeItemCodes());
        Set<String> supportedChangeItems = codeDescriptionService.findChangeItems().stream()
                .map(code -> code.getCodeBefore())
                .collect(Collectors.toSet());
        changeItemCodes.stream()
                .filter(changeItemCode -> !supportedChangeItems.contains(changeItemCode))
                .findFirst()
                .ifPresent(changeItemCode -> {
                    throw new IllegalArgumentException("不支援的保全變更項目: " + changeItemCode);
                });

        changeItemCodes.stream()
                .map(changeItemCode -> checkEligibility(request.getPolicyNo(), request.getPolicySeq(), changeItemCode))
                .filter(eligibility -> !eligibility.isEligible())
                .findFirst()
                .ifPresent(eligibility -> {
                    log.warn(
                            "保全申請遭受理中案件阻擋 policyNo={}, policySeq={}, changeItemCode={}, latestChangeCaseNo={}",
                            maskPolicyNo(request.getPolicyNo()),
                            request.getPolicySeq(),
                            eligibility.getChangeItemCode(),
                            eligibility.getLatestChangeCaseNo()
                    );
                    throw new ChangeCaseConflictException(PENDING_APPLICATION_MESSAGE);
                });

        String changeCaseNo = generateChangeCaseNo();
        LocalDateTime now = LocalDateTime.now(changeCaseZoneId);
        policyChangeDao.insertCaseReservation(PolicyChangeCaseReservation.builder()
                .changeCaseNo(changeCaseNo)
                .policyNo(request.getPolicyNo())
                .policySeq(request.getPolicySeq())
                .reservedBy(currentUserService.username())
                .expiresAt(now.plus(reservationTtl))
                .build());
        changeItemCodes.forEach(changeItemCode -> policyChangeDao.insertCaseReservationItem(
                PolicyChangeCaseReservationItem.builder()
                        .changeCaseNo(changeCaseNo)
                        .changeItemCode(changeItemCode)
                        .build()
        ));
        log.info(
                "保全案號保留成功 policyNo={}, policySeq={}, changeCaseNo={}, changeItemCodes={}",
                maskPolicyNo(request.getPolicyNo()),
                request.getPolicySeq(),
                changeCaseNo,
                changeItemCodes
        );

        return CreateChangeCaseDto.builder()
                .policyNo(request.getPolicyNo())
                .policySeq(request.getPolicySeq())
                .changeCaseNo(changeCaseNo)
                .acceptanceStatus(codeDescriptionService.pendingStatusCode())
                .changeItemCodes(changeItemCodes)
                .build();
    }

    private List<String> normalizeChangeItems(List<String> requestedChangeItems) {
        List<String> changeItemCodes = requestedChangeItems.stream()
                .peek(changeItemCode -> requireText(changeItemCode, "changeItemCode"))
                .map(String::trim)
                .toList();
        if (new LinkedHashSet<>(changeItemCodes).size() != changeItemCodes.size()) {
            throw new IllegalArgumentException("changeItemCodes 不可重複");
        }
        return List.copyOf(changeItemCodes);
    }

    private String generateChangeCaseNo() {
        LocalDate today = LocalDate.now(changeCaseZoneId);
        if (policyChangeDao.incrementCaseSequence(today) < 1) {
            throw new IllegalStateException("更新變更案號流水號失敗");
        }
        Long nextSerial = policyChangeDao.findLastInsertedSequence();
        if (nextSerial == null || nextSerial < 1) {
            throw new IllegalStateException("無法取得今日變更案號流水號");
        }
        return buildCaseNoPrefix(today) + String.format("%03d", nextSerial);
    }

    private String buildCaseNoPrefix(LocalDate today) {
        int rocYear = today.getYear() - 1911;
        return "C" + String.format("%03d", rocYear) + today.format(MONTH_DAY_FORMATTER);
    }
}
