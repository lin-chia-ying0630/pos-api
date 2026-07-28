package com.alin.lin.service;

import com.alin.lin.config.PosChangeProperties;
import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.entity.PolicyChangeAcceptance;
import com.alin.lin.entity.PolicyChangeCaseReservation;
import com.alin.lin.entity.PolicyChangeItem;
import com.alin.lin.exception.ChangeCaseConflictException;
import com.alin.lin.service.impl.PolicyChangeSupportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PolicyChangeSupportServiceImplTest {
    private static final ZoneId CHANGE_CASE_ZONE_ID = ZoneId.of("Asia/Taipei");
    private PolicyChangeDao dao;
    private CodeDescriptionService codeService;
    private CurrentUserService currentUserService;
    private PolicyChangeSupportServiceImpl service;

    @BeforeEach
    void setUp() {
        dao = mock(PolicyChangeDao.class);
        codeService = mock(CodeDescriptionService.class);
        currentUserService = mock(CurrentUserService.class);
        PosChangeProperties properties = new PosChangeProperties();
        properties.setZoneId("Asia/Taipei");
        service = new PolicyChangeSupportServiceImpl(dao, codeService, currentUserService, properties);

        when(currentUserService.username()).thenReturn("maker-a");
        when(currentUserService.securityEnabled()).thenReturn(true);
        when(codeService.pendingStatusCode()).thenReturn("P");
    }

    @Test
    void rejectsReservationOwnedByAnotherMaker() {
        when(dao.findCaseReservationForUpdate("C1150718001")).thenReturn(reservation("maker-b", now().plusMinutes(30)));
        when(dao.findReservedChangeItems("C1150718001")).thenReturn(java.util.List.of("001"));

        assertThatThrownBy(() -> service.validateChangeCaseAccess("P000000001", 1, "C1150718001", "001"))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("不是由目前帳號產生");
    }

    @Test
    void rejectsExpiredReservation() {
        when(dao.findCaseReservationForUpdate("C1150718001")).thenReturn(reservation("maker-a", now().minusMinutes(1)));
        when(dao.findReservedChangeItems("C1150718001")).thenReturn(java.util.List.of("001"));

        assertThatThrownBy(() -> service.validateChangeCaseAccess("P000000001", 1, "C1150718001", "001"))
                .isInstanceOf(ChangeCaseConflictException.class)
                .hasMessageContaining("已逾期");
    }

    @Test
    void consumesReservationAndRecordsCreatorWhenFirstChangeIsSaved() {
        when(dao.findCaseReservationForUpdate("C1150718001")).thenReturn(reservation("maker-a", now().plusMinutes(30)));
        when(dao.findReservedChangeItems("C1150718001")).thenReturn(java.util.List.of("001", "002"));
        when(dao.insertAcceptance(any())).thenReturn(1);
        when(dao.insertChangeItem(any())).thenReturn(1);
        when(dao.consumeCaseReservation("C1150718001", "maker-a")).thenReturn(1);

        service.ensureChangeCaseSaved("P000000001", 1, "C1150718001", "001");

        ArgumentCaptor<PolicyChangeAcceptance> acceptanceCaptor = ArgumentCaptor.forClass(PolicyChangeAcceptance.class);
        verify(dao).insertAcceptance(acceptanceCaptor.capture());
        verify(dao).consumeCaseReservation("C1150718001", "maker-a");
        assertThat(acceptanceCaptor.getValue().getCreatedBy()).isEqualTo("maker-a");
        assertThat(acceptanceCaptor.getValue().getAcceptanceStatus()).isEqualTo("P");
    }

    @Test
    void addsSecondSelectedItemWithoutCreatingAnotherAcceptance() {
        PolicyChangeAcceptance acceptance = PolicyChangeAcceptance.builder()
                .policyNo("P000000001")
                .policySeq(1)
                .changeCaseNo("C1150718001")
                .acceptanceStatus("P")
                .createdBy("maker-a")
                .build();
        when(dao.findAcceptanceForUpdate("P000000001", 1, "C1150718001")).thenReturn(acceptance);
        when(dao.findChangeItemsByCaseNo("P000000001", 1, "C1150718001"))
                .thenReturn(java.util.List.of("001"));
        when(dao.findCaseReservationForUpdate("C1150718001"))
                .thenReturn(reservation("maker-a", now().plusMinutes(30)));
        when(dao.findReservedChangeItems("C1150718001")).thenReturn(java.util.List.of("001", "002"));

        service.ensureChangeCaseSaved("P000000001", 1, "C1150718001", "002");

        ArgumentCaptor<PolicyChangeItem> itemCaptor = ArgumentCaptor.forClass(PolicyChangeItem.class);
        verify(dao).insertChangeItem(itemCaptor.capture());
        verify(dao, never()).insertAcceptance(any());
        verify(dao, never()).consumeCaseReservation(any(), any());
        assertThat(itemCaptor.getValue().getChangeItemCode()).isEqualTo("002");
    }

    private PolicyChangeCaseReservation reservation(String reservedBy, LocalDateTime expiresAt) {
        return PolicyChangeCaseReservation.builder()
                .changeCaseNo("C1150718001")
                .policyNo("P000000001")
                .policySeq(1)
                .reservedBy(reservedBy)
                .expiresAt(expiresAt)
                .build();
    }

    private LocalDateTime now() {
        return LocalDateTime.now(CHANGE_CASE_ZONE_ID);
    }
}
