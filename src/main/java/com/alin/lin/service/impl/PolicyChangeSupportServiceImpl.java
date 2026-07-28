package com.alin.lin.service.impl;

import com.alin.lin.config.PosChangeProperties;
import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.entity.PolicyContract;
import com.alin.lin.entity.PolicyCoverage;
import com.alin.lin.entity.PolicyChangeAcceptance;
import com.alin.lin.entity.PolicyChangeCaseReservation;
import com.alin.lin.entity.PolicyChangeItem;
import com.alin.lin.enums.PolicyRideKey;
import com.alin.lin.exception.ChangeCaseConflictException;
import com.alin.lin.service.CodeDescriptionService;
import com.alin.lin.service.CurrentUserService;
import com.alin.lin.service.PolicyChangeSupportService;
import com.alin.lin.util.PolicyChangeFieldUtil.FieldChange;
import org.springframework.stereotype.Service;
import com.alin.lin.util.UuidV7;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

import static com.alin.lin.util.PolicyChangeFieldUtil.requireNotNull;
import static com.alin.lin.util.PolicyChangeFieldUtil.requireText;

@Service
public class PolicyChangeSupportServiceImpl implements PolicyChangeSupportService {
    private final PolicyChangeDao policyChangeDao;
    private final CodeDescriptionService codeDescriptionService;
    private final CurrentUserService currentUserService;
    private final ZoneId changeCaseZoneId;

    public PolicyChangeSupportServiceImpl(
            PolicyChangeDao policyChangeDao,
            CodeDescriptionService codeDescriptionService,
            CurrentUserService currentUserService,
            PosChangeProperties posChangeProperties
    ) {
        this.policyChangeDao = policyChangeDao;
        this.codeDescriptionService = codeDescriptionService;
        this.currentUserService = currentUserService;
        this.changeCaseZoneId = ZoneId.of(posChangeProperties.getZoneId());
    }

    @Override
    public PolicyContract requirePolicy(String policyNo, Integer policySeq) {
        requireText(policyNo, "policyNo");
        requireNotNull(policySeq, "policySeq");
        PolicyContract master = policyChangeDao.findMaster(policyNo, policySeq);
        if (master == null) {
            throw new NoSuchElementException("找不到保單: " + policyNo + "-" + policySeq);
        }
        return master;
    }

    @Override
    public PolicyCoverage requireMainRide(String policyNo, Integer policySeq) {
        return policyChangeDao.findRides(policyNo, policySeq).stream()
                .filter(ride -> codeDescriptionService.mainRideTypeCode().equals(ride.getCoverageItemType())
                        || PolicyRideKey.MAIN.getCoverageItemSeq().equals(ride.getCoverageItemSeq()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("找不到主約資料: " + policyNo + "-" + policySeq));
    }

    @Override
    public void validateChangeCaseAccess(
            String policyNo,
            Integer policySeq,
            String changeCaseNo,
            String changeItemCode
    ) {
        requireText(policyNo, "policyNo");
        requireNotNull(policySeq, "policySeq");
        requireText(changeCaseNo, "changeCaseNo");
        requireText(changeItemCode, "changeItemCode");

        String username = currentUserService.username();
        PolicyChangeAcceptance acceptance = policyChangeDao.findAcceptanceForUpdate(policyNo, policySeq, changeCaseNo);
        if (acceptance != null) {
            requireAcceptanceOwner(acceptance, username);
            if (!codeDescriptionService.pendingStatusCode().equals(acceptance.getAcceptanceStatus())) {
                throw new ChangeCaseConflictException("只有 P-受理中的案件可以修改");
            }
            List<String> existingItems = policyChangeDao.findChangeItemsByCaseNo(policyNo, policySeq, changeCaseNo);
            if (existingItems.contains(changeItemCode)) {
                return;
            }
            requireReservedChangeItem(policyNo, policySeq, changeCaseNo, changeItemCode, username, false);
            return;
        }

        requireReservedChangeItem(policyNo, policySeq, changeCaseNo, changeItemCode, username, true);
    }

    private PolicyChangeCaseReservation requireReservedChangeItem(
            String policyNo,
            Integer policySeq,
            String changeCaseNo,
            String changeItemCode,
            String username,
            boolean requireNotExpired
    ) {
        PolicyChangeCaseReservation reservation = policyChangeDao.findCaseReservationForUpdate(changeCaseNo);
        if (reservation == null) {
            throw new NoSuchElementException("找不到有效的變更案號: " + changeCaseNo);
        }
        if (!Objects.equals(policyNo, reservation.getPolicyNo())
                || !Objects.equals(policySeq, reservation.getPolicySeq())) {
            throw new IllegalArgumentException("案號與保單不符");
        }
        if (!policyChangeDao.findReservedChangeItems(changeCaseNo).contains(changeItemCode)) {
            throw new IllegalArgumentException("此案號未選擇保全變更項目: " + changeItemCode);
        }
        if (requireNotExpired && !reservation.getExpiresAt().isAfter(LocalDateTime.now(changeCaseZoneId))) {
            throw new ChangeCaseConflictException("變更案號已逾期，請重新產生案號");
        }
        if (!Objects.equals(username, reservation.getReservedBy())) {
            throw new AccessDeniedException("此變更案號不是由目前帳號產生");
        }
        return reservation;
    }

    @Override
    public void ensureChangeCaseSaved(String policyNo, Integer policySeq, String changeCaseNo, String changeItemCode) {
        validateChangeCaseAccess(policyNo, policySeq, changeCaseNo, changeItemCode);
        if (policyChangeDao.existsChangeItem(policyNo, policySeq, changeCaseNo, changeItemCode) > 0) {
            return;
        }
        PolicyChangeAcceptance acceptance = policyChangeDao.findAcceptanceForUpdate(policyNo, policySeq, changeCaseNo);
        if (acceptance == null) {
            policyChangeDao.insertAcceptance(PolicyChangeAcceptance.builder()
                    .changeCaseId(UuidV7.next())
                    .policyNo(policyNo)
                    .policySeq(policySeq)
                    .changeCaseNo(changeCaseNo)
                    .acceptanceStatus(codeDescriptionService.pendingStatusCode())
                    .createdBy(currentUserService.username())
                    .build());

            PolicyChangeCaseReservation reservation = policyChangeDao.findCaseReservationForUpdate(changeCaseNo);
            if (reservation.getConsumedAt() == null
                    && policyChangeDao.consumeCaseReservation(changeCaseNo, currentUserService.username()) != 1) {
                throw new ChangeCaseConflictException("變更案號已失效，請重新產生案號");
            }
        }
        policyChangeDao.insertChangeItem(PolicyChangeItem.builder()
                .changeItemId(UuidV7.next())
                .policyNo(policyNo)
                .policySeq(policySeq)
                .changeCaseNo(changeCaseNo)
                .changeItemCode(changeItemCode)
                .build());
    }

    @Override
    public void upsertFieldChange(String policyNo, Integer policySeq, String changeCaseNo, String changeItemCode, FieldChange fieldChange) {
        policyChangeDao.upsertChangeField(
                UuidV7.next(),
                policyNo,
                policySeq,
                changeCaseNo,
                changeItemCode,
                fieldChange.field(),
                fieldChange.key(),
                fieldChange.beforeValue(),
                fieldChange.afterValue()
        );
    }

    @Override
    public void removeEmptyChangeItemAndAcceptance(String policyNo, Integer policySeq, String changeCaseNo, String changeItemCode) {
        int fieldCount = policyChangeDao.countChangeFieldsByItem(policyNo, policySeq, changeCaseNo, changeItemCode);
        int fileCount = policyChangeDao.countChangeFilesByItem(policyNo, policySeq, changeCaseNo, changeItemCode);
        if (fieldCount > 0 || fileCount > 0) {
            return;
        }

        policyChangeDao.deleteChangeItem(policyNo, policySeq, changeCaseNo, changeItemCode);
        if (policyChangeDao.countChangeItemsByCaseNo(policyNo, policySeq, changeCaseNo) == 0) {
            policyChangeDao.deleteAcceptance(
                    policyNo,
                    policySeq,
                    changeCaseNo,
                    codeDescriptionService.pendingStatusCode()
            );
        }
    }

    private void requireAcceptanceOwner(PolicyChangeAcceptance acceptance, String username) {
        if (currentUserService.securityEnabled() && !Objects.equals(username, acceptance.getCreatedBy())) {
            throw new AccessDeniedException("只有原建檔經辦可以修改此案件");
        }
    }
}
