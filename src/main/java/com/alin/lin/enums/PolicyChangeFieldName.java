package com.alin.lin.enums;

import lombok.Getter;

@Getter
public enum PolicyChangeFieldName {
    // 完整郵遞區號（canonical）
    POSTAL_CODE("postal_code"),

    // 地址郵遞區號前三碼
    ZIP_CODE3("zip_code3"),

    // 地址郵遞區號後三碼
    ZIP_CODE2("zip_code2"),

    // 地址
    FULL_WIDTH_ADDRESS("full_width_address"),

    // email / 電話 / 手機
    HALF_WIDTH_ADDRESS("half_width_address");

    private final String fieldName;

    PolicyChangeFieldName(String fieldName) {
        this.fieldName = fieldName;
    }
}
