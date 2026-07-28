package com.alin.lin.util;

import com.alin.lin.entity.PolicyContact;
import com.alin.lin.enums.PolicyChangeFieldName;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class PolicyChangeFieldUtil {
    private PolicyChangeFieldUtil() {
    }

    public static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " 不可空白");
        }
    }

    public static <T> T requireNotNull(T value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " 不可空白");
        }
        return value;
    }

    public static <T extends Collection<?>> T requireNotEmpty(T value, String fieldName) {
        if (value == null || value.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " 不可空白");
        }
        return value;
    }

    public static String normalizeBlank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public static void validateAddressPostalCodeFormat(String postalCode) {
        if (postalCode == null || !postalCode.matches("^\\d{3}(?:\\d{3})?$")) {
            throw new IllegalArgumentException("郵遞區號必須為 3 或 6 碼數字");
        }
    }

    public static List<FieldChange> collectAddressFieldChanges(PolicyContact beforeAddress, PolicyContact afterAddress) {
        List<FieldChange> fieldChanges = new ArrayList<>();
        String key = beforeAddress.getAddressId() == null
                ? beforeAddress.getAddressTypeCode()
                : beforeAddress.getAddressId();
        addTextChangeIfDifferent(fieldChanges, PolicyChangeFieldName.POSTAL_CODE.getFieldName(), key, beforeAddress.getPostalCode(), afterAddress.getPostalCode());
        addTextChangeIfDifferent(fieldChanges, PolicyChangeFieldName.FULL_WIDTH_ADDRESS.getFieldName(), key, beforeAddress.getAddressText(), afterAddress.getAddressText());
        return fieldChanges;
    }

    public static String canonicalPostalCode(PolicyContact address) {
        return normalizeBlank(address.getPostalCode());
    }

    public static String canonicalAddress(PolicyContact address) {
        return normalizeBlank(address.getAddressText());
    }

    public static void addAmountChangeIfDifferent(List<FieldChange> fieldChanges, String field, String key, BigDecimal beforeValue, BigDecimal afterValue) {
        if (!amountEquals(beforeValue, afterValue)) {
            fieldChanges.add(new FieldChange(field, key, amountToString(beforeValue), amountToString(afterValue)));
        }
    }

    public static boolean amountEquals(BigDecimal beforeValue, BigDecimal afterValue) {
        return beforeValue != null && afterValue != null && beforeValue.compareTo(afterValue) == 0;
    }

    public static String amountToString(BigDecimal value) {
        return value == null ? null : value.stripTrailingZeros().toPlainString();
    }

    private static void addTextChangeIfDifferent(List<FieldChange> fieldChanges, String field, String key, String beforeValue, String afterValue) {
        String normalizedBefore = normalizeBlank(beforeValue);
        String normalizedAfter = normalizeBlank(afterValue);
        if (!fieldValueEquals(field, normalizedBefore, normalizedAfter)) {
            fieldChanges.add(new FieldChange(field, key, normalizedBefore, normalizedAfter));
        }
    }

    private static boolean fieldValueEquals(String field, String beforeValue, String afterValue) {
        if (PolicyChangeFieldName.ZIP_CODE2.getFieldName().equals(field)) {
            return Objects.equals(normalizeZipCodeForCompare(beforeValue), normalizeZipCodeForCompare(afterValue));
        }
        if (PolicyChangeFieldName.FULL_WIDTH_ADDRESS.getFieldName().equals(field)
                || PolicyChangeFieldName.HALF_WIDTH_ADDRESS.getFieldName().equals(field)) {
            return Objects.equals(normalizeAddressForCompare(beforeValue), normalizeAddressForCompare(afterValue));
        }
        return Objects.equals(beforeValue, afterValue);
    }

    private static String normalizeZipCodeForCompare(String value) {
        if (value == null) {
            return null;
        }
        return value.length() >= 3 ? value : "0".repeat(3 - value.length()) + value;
    }

    private static String normalizeAddressForCompare(String value) {
        if (value == null) {
            return null;
        }
        StringBuilder result = new StringBuilder();
        for (char sourceChar : value.toCharArray()) {
            if (Character.isWhitespace(sourceChar)) {
                continue;
            }
            if (sourceChar >= '０' && sourceChar <= '９') {
                result.append((char) ('0' + sourceChar - '０'));
                continue;
            }
            if (sourceChar >= 'Ａ' && sourceChar <= 'Ｚ') {
                result.append((char) ('A' + sourceChar - 'Ａ'));
                continue;
            }
            if (sourceChar >= 'ａ' && sourceChar <= 'ｚ') {
                result.append((char) ('a' + sourceChar - 'ａ'));
                continue;
            }
            result.append(sourceChar);
        }
        return result.toString();
    }

    public record FieldChange(String field, String key, String beforeValue, String afterValue) {
    }
}
