package com.alin.lin.util;

import com.alin.lin.entity.PolicyContact;
import com.alin.lin.enums.PolicyChangeFieldName;
import com.alin.lin.enums.PolicyRideKey;
import com.alin.lin.enums.RideChangeField;
import com.alin.lin.util.PolicyChangeFieldUtil.FieldChange;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PolicyChangeFieldUtilTest {
    @Test
    void collectAddressFieldChangesUsesCanonicalPostalCode() {
        PolicyContact beforeAddress = address("01", "100", "001", "臺北市中正區重慶南路一段１00號", "No.1, Sec.100, Chongqing S. Rd.");
        PolicyContact afterAddress = address("01", "100", "001", "臺北市中正區重慶南路一段１00號", "No.1, Sec.100, Chongqing S. Rd.");

        List<FieldChange> changes = PolicyChangeFieldUtil.collectAddressFieldChanges(beforeAddress, afterAddress);

        assertTrue(changes.isEmpty());
    }

    @Test
    void collectAddressFieldChangesIgnoresAddressWhitespaceAndFullWidthAlphaNumericDifferences() {
        PolicyContact beforeAddress = address("01", "100", "001", "臺北市中正區重慶南路一段１00號", "No.Ａ, Sec.100, Taipei City");
        PolicyContact afterAddress = address("01", "100", "001", "臺北市 中正區 重慶南路一段100號", "No.A,Sec.100,TaipeiCity");

        List<FieldChange> changes = PolicyChangeFieldUtil.collectAddressFieldChanges(beforeAddress, afterAddress);

        assertTrue(changes.isEmpty());
    }

    @Test
    void collectAddressFieldChangesReturnsOnlyActuallyChangedAddressFields() {
        PolicyContact beforeAddress = address("01", "100", "001", "臺北市中正區重慶南路一段100號", "No.1, Sec.100, Taipei City");
        PolicyContact afterAddress = address("01", "104", "001", "臺北市中山區南京東路二段100號", "No.100, Sec.2, Taipei City");

        List<FieldChange> changes = PolicyChangeFieldUtil.collectAddressFieldChanges(beforeAddress, afterAddress);

        assertEquals(2, changes.size());
        assertEquals(PolicyChangeFieldName.POSTAL_CODE.getFieldName(), changes.get(0).field());
        assertEquals(PolicyChangeFieldName.FULL_WIDTH_ADDRESS.getFieldName(), changes.get(1).field());
    }

    @Test
    void amountEqualsIgnoresScaleDifferences() {
        assertTrue(PolicyChangeFieldUtil.amountEquals(new BigDecimal("1000000.00"), new BigDecimal("1000000")));
    }

    @Test
    void addAmountChangeIfDifferentDoesNotAddChangeWhenAmountsAreEqual() {
        List<FieldChange> changes = new ArrayList<>();

        PolicyChangeFieldUtil.addAmountChangeIfDifferent(
                changes,
                RideChangeField.INSURED_AMOUNT.fieldName(PolicyRideKey.MAIN.getCoverageItemSeq()),
                PolicyRideKey.MAIN.getCoverageItemSeq(),
                new BigDecimal("1000000.00"),
                new BigDecimal("1000000")
        );

        assertTrue(changes.isEmpty());
    }

    @Test
    void validateAddressPostalCodeFormatAllowsThreeDigits() {
        assertDoesNotThrow(() -> PolicyChangeFieldUtil.validateAddressPostalCodeFormat("104"));
    }

    @Test
    void validateAddressPostalCodeFormatRejectsInvalidLength() {
        assertThrows(IllegalArgumentException.class, () -> PolicyChangeFieldUtil.validateAddressPostalCodeFormat("10401"));
    }

    private PolicyContact address(String addressTypeCode, String zipCode3, String zipCode2, String fullWidthAddress, String halfWidthAddress) {
        return PolicyContact.builder()
                .policyNo("P000000001")
                .policySeq(1)
                .addressTypeCode(addressTypeCode)
                .postalCode(zipCode3 + (zipCode2 == null ? "" : zipCode2))
                .addressText(fullWidthAddress)
                .build();
    }
}
