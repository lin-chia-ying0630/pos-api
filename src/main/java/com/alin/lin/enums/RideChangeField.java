package com.alin.lin.enums;

import lombok.Getter;

@Getter
public enum RideChangeField {
    // 主附約保額
    INSURED_AMOUNT(".insured_amount"),

    // 主附約保費
    PREMIUM(".premium_amount", ".premium");

    private static final String PREFIX = "policy_coverage.";
    private static final String LEGACY_PREFIX = "main_policy_ride.";

    private final String suffix;
    private final String legacySuffix;

    RideChangeField(String suffix) {
        this(suffix, suffix);
    }

    RideChangeField(String suffix, String legacySuffix) {
        this.suffix = suffix;
        this.legacySuffix = legacySuffix;
    }

    public String fieldName(String coverageItemSeq) {
        return PREFIX + coverageItemSeq + suffix;
    }

    public boolean matches(String fieldName) {
        return fieldName != null && ((fieldName.startsWith(PREFIX) && fieldName.endsWith(suffix))
                || (fieldName.startsWith(LEGACY_PREFIX) && fieldName.endsWith(legacySuffix)));
    }

    public String resolveRideOrder(String fieldName) {
        // 待覆核歷史資料可能仍使用 main_policy_ride 與 premium，必須保留讀取相容。
        boolean legacy = fieldName.startsWith(LEGACY_PREFIX);
        String prefix = legacy ? LEGACY_PREFIX : PREFIX;
        String fieldSuffix = legacy ? legacySuffix : suffix;
        return fieldName.substring(prefix.length(), fieldName.length() - fieldSuffix.length());
    }
}
