package com.alin.lin.service;

import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.entity.PolicyChangeFile;
import com.alin.lin.entity.PolicyContact;
import com.alin.lin.service.impl.ChangeCaseApplyServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ChangeCaseApplyServiceImplTest {
    @Test
    void appliesCanonicalAddressSnapshot() {
        PolicyChangeDao dao = mock(PolicyChangeDao.class);
        CodeDescriptionService codes = mock(CodeDescriptionService.class);
        ChangeCaseApplyServiceImpl service = new ChangeCaseApplyServiceImpl(dao, codes, new ObjectMapper());
        String before = """
                {"policyNo":"P000000001","policySeq":1,"addressTypeCode":"01",
                 "postalCode":"100001","addressText":"舊地址"}
                """;
        String after = """
                {"policyNo":"P000000001","policySeq":1,"addressTypeCode":"01",
                 "postalCode":"100001","addressText":"新地址"}
                """;

        when(codes.addressChangeItemCode()).thenReturn("001");
        when(codes.mainAmountChangeItemCode()).thenReturn("002");
        when(codes.riderAmountChangeItemCode()).thenReturn("003");
        when(dao.findChangeItemsByCaseNo("P000000001", 1, "C1150718012")).thenReturn(List.of("001"));
        when(dao.findChangeFilesByItem("P000000001", 1, "C1150718012", "001")).thenReturn(List.of(
                PolicyChangeFile.builder().changedRecordKey("01").contentBefore(before).contentAfter(after).build()));
        when(dao.findAddressForUpdate("P000000001", 1, "01")).thenReturn(PolicyContact.builder()
                .policyNo("P000000001").policySeq(1).addressTypeCode("01")
                .postalCode("100001").addressText("舊地址").build());
        when(dao.updateAddress(argThat(address -> "01".equals(address.getAddressTypeCode())
                && "新地址".equals(address.getAddressText())))).thenReturn(1);

        assertThat(service.applyChangeCase("P000000001", 1, "C1150718012")).isEqualTo(1);
    }

    @Test
    void fallsBackToSnapshotRecordKeyWhenJsonHasNoAddressType() {
        PolicyChangeDao dao = mock(PolicyChangeDao.class);
        CodeDescriptionService codes = mock(CodeDescriptionService.class);
        ChangeCaseApplyServiceImpl service = new ChangeCaseApplyServiceImpl(dao, codes, new ObjectMapper());
        String before = "{\"policyNo\":\"P000000001\",\"policySeq\":1,\"addressText\":\"舊地址\"}";
        String after = "{\"policyNo\":\"P000000001\",\"policySeq\":1,\"addressText\":\"新地址\"}";

        when(codes.addressChangeItemCode()).thenReturn("001");
        when(codes.mainAmountChangeItemCode()).thenReturn("002");
        when(codes.riderAmountChangeItemCode()).thenReturn("003");
        when(dao.findChangeItemsByCaseNo("P000000001", 1, "C1150718012")).thenReturn(List.of("001"));
        when(dao.findChangeFilesByItem("P000000001", 1, "C1150718012", "001")).thenReturn(List.of(
                PolicyChangeFile.builder().changedRecordKey("01").contentBefore(before).contentAfter(after).build()));
        when(dao.findAddressForUpdate("P000000001", 1, "01")).thenReturn(PolicyContact.builder()
                .policyNo("P000000001").policySeq(1).addressTypeCode("01").addressText("舊地址").build());
        when(dao.updateAddress(argThat(address -> "01".equals(address.getAddressTypeCode())))).thenReturn(1);

        assertThat(service.applyChangeCase("P000000001", 1, "C1150718012")).isEqualTo(1);
    }
}
