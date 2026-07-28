package com.alin.lin.service;

import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.dto.PolicyChangeCaseDto;
import com.alin.lin.dto.PolicyChangeCaseDetailDto;
import com.alin.lin.entity.PolicyContract;
import com.alin.lin.entity.PolicyContact;
import com.alin.lin.entity.PolicyChangeFile;
import com.alin.lin.entity.PolicyChangeField;
import com.alin.lin.service.impl.PolicyQueryServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PolicyQueryServiceImplTest {
    @Test
    void returnsPolicyDetailWhenCommunicationAddressCodeIsMissing() {
        PolicyChangeDao dao = mock(PolicyChangeDao.class);
        PolicyChangeSupportService supportService = mock(PolicyChangeSupportService.class);
        CodeDescriptionService codeService = mock(CodeDescriptionService.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        PolicyQueryServiceImpl service = new PolicyQueryServiceImpl(
                dao, supportService, codeService, currentUserService, new ObjectMapper());
        PolicyContract master = PolicyContract.builder().policyNo("P000000001").policySeq(1).build();
        PolicyContact address = PolicyContact.builder()
                .policyNo("P000000001").policySeq(1).addressTypeCode("99").build();
        when(supportService.requirePolicy("P000000001", 1)).thenReturn(master);
        when(dao.findAddresses("P000000001", 1)).thenReturn(List.of(address));
        when(dao.findRides("P000000001", 1)).thenReturn(List.of());
        when(codeService.findCommunicationAddressCode()).thenReturn(Optional.empty());
        when(codeService.findAddressTypes()).thenReturn(List.of());
        when(codeService.findAcceptanceStatuses()).thenReturn(List.of());
        when(codeService.findChangeItems()).thenReturn(List.of());
        when(codeService.findScreenPermissions()).thenReturn(List.of());

        var result = service.findPolicyDetail("P000000001", 1);

        assertThat(result.getMaster()).isSameAs(master);
        assertThat(result.getAddressList()).containsExactly(address);
        assertThat(result.getCommunicationAddress()).isNull();
    }

    @Test
    void splitsSnapshotJsonAndUsesChtCodeFieldName() {
        PolicyChangeDao dao = mock(PolicyChangeDao.class);
        PolicyChangeSupportService supportService = mock(PolicyChangeSupportService.class);
        CodeDescriptionService codeService = mock(CodeDescriptionService.class);
        CurrentUserService currentUserService = mock(CurrentUserService.class);
        PolicyQueryServiceImpl service = new PolicyQueryServiceImpl(
                dao,
                supportService,
                codeService,
                currentUserService,
                new ObjectMapper()
        );
        when(currentUserService.securityEnabled()).thenReturn(false);

        when(supportService.requirePolicy("P000000001", 1)).thenReturn(new PolicyContract());
        when(dao.findChangeCase("P000000001", 1, "C001")).thenReturn(
                PolicyChangeCaseDto.builder().changeCaseNo("C001").build()
        );
        when(dao.findChangeFieldsByCaseNo("P000000001", 1, "C001")).thenReturn(List.of(
                PolicyChangeField.builder().changedFieldName("half_width_address").build()
        ));
        when(dao.findChangeFilesByCaseNo("P000000001", 1, "C001")).thenReturn(List.of(
                PolicyChangeFile.builder()
                        .id(1L)
                        .contentBefore("{\"policyNo\":\"P000000001\",\"zipCode3\":null}")
                        .contentAfter("{\"policyNo\":\"P000000001\",\"zipCode3\":\"100\"}")
                        .build()
        ));
        when(codeService.findChtFieldNames()).thenReturn(Map.of(
                "policyNo", "保單號碼",
                "zipCode3", "郵遞區號前三碼",
                "halfWidthAddress", "電子郵件／電話／手機"
        ));

        PolicyChangeCaseDetailDto result = service.findChangeCaseDetail("P000000001", 1, "C001");

        assertThat(result.getChangedRecordTypes().get(0).getSnapshotFields())
                .extracting("jsonKey", "chineseName", "contentBefore", "contentAfter")
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple("policyNo", "保單號碼", "P000000001", "P000000001"),
                        org.assertj.core.groups.Tuple.tuple("zipCode3", "郵遞區號前三碼", null, "100")
                );
        assertThat(result.getChangedFieldNames().get(0).getChineseName())
                .isEqualTo("電子郵件／電話／手機");
    }
}
