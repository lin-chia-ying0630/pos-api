package com.alin.lin.service;

import com.alin.lin.config.PosChangeProperties;
import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.dto.CreateChangeCaseDto;
import com.alin.lin.dto.CreateChangeCaseRequest;
import com.alin.lin.entity.CodeDescription;
import com.alin.lin.entity.PolicyChangeCaseReservation;
import com.alin.lin.dto.PolicyChangeCaseDto;
import com.alin.lin.exception.ChangeCaseConflictException;
import com.alin.lin.service.impl.ChangeCaseDraftServiceImpl;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

class ChangeCaseDraftServiceImplTest {
    @Test
    void reservesDatabaseSequenceAndAllowsMoreThanThreeSerialDigits() {
        PolicyChangeDao dao = mock(PolicyChangeDao.class);
        PolicyChangeSupportService supportService = mock(PolicyChangeSupportService.class);
        CodeDescriptionService codeService = mock(CodeDescriptionService.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        PosChangeProperties properties = new PosChangeProperties();
        properties.setZoneId("Asia/Taipei");
        properties.setReservationTtl(Duration.ofMinutes(30));
        when(dao.incrementCaseSequence(any(LocalDate.class))).thenReturn(2);
        when(dao.findLastInsertedSequence()).thenReturn(1000L);
        when(codeService.pendingStatusCode()).thenReturn("P");
        when(codeService.findChangeItems()).thenReturn(List.of(
                CodeDescription.builder().codeBefore("001").build(),
                CodeDescription.builder().codeBefore("002").build()
        ));
        when(currentUserService.username()).thenReturn("maker");

        ChangeCaseDraftServiceImpl service = new ChangeCaseDraftServiceImpl(
                dao, supportService, codeService, currentUserService, properties
        );
        CreateChangeCaseDto result = service.createChangeCase(CreateChangeCaseRequest.builder()
                .policyNo("P000000001")
                .policySeq(1)
                .changeItemCodes(List.of("001", "002"))
                .build());

        assertTrue(result.getChangeCaseNo().endsWith("1000"));
        assertEquals("P", result.getAcceptanceStatus());
        assertEquals(List.of("001", "002"), result.getChangeItemCodes());
        verify(dao).incrementCaseSequence(any(LocalDate.class));
        verify(dao).findLastInsertedSequence();
        verify(dao).insertCaseReservation(any(PolicyChangeCaseReservation.class));
        verify(dao, times(2)).insertCaseReservationItem(any());
    }

    @Test
    void rejectsNewCaseWhenLatestSameItemIsPending() {
        PolicyChangeDao dao = mock(PolicyChangeDao.class);
        PolicyChangeSupportService supportService = mock(PolicyChangeSupportService.class);
        CodeDescriptionService codeService = mock(CodeDescriptionService.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        PosChangeProperties properties = new PosChangeProperties();
        properties.setZoneId("Asia/Taipei");
        properties.setReservationTtl(Duration.ofMinutes(30));
        when(codeService.pendingStatusCode()).thenReturn("P");
        when(codeService.findChangeItems()).thenReturn(List.of(
                CodeDescription.builder().codeBefore("001").build()
        ));
        when(dao.findLatestChangeCaseByItem("P000000001", 1, "001")).thenReturn(
                PolicyChangeCaseDto.builder()
                        .changeCaseNo("C1150718001")
                        .acceptanceStatus("P")
                        .build()
        );

        ChangeCaseDraftServiceImpl service = new ChangeCaseDraftServiceImpl(
                dao, supportService, codeService, currentUserService, properties
        );

        ChangeCaseConflictException exception = assertThrows(
                ChangeCaseConflictException.class,
                () -> service.createChangeCase(CreateChangeCaseRequest.builder()
                        .policyNo("P000000001")
                        .policySeq(1)
                        .changeItemCodes(List.of("001"))
                        .build())
        );

        assertEquals("此保單正在受理中，無法申請", exception.getMessage());
    }
}
