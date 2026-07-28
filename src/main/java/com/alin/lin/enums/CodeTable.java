package com.alin.lin.enums;

import lombok.Getter;

@Getter
public enum CodeTable {
    // 地址型態
    ADDRESS_TYPE("policy-contact", "address_type_code"),

    // 附約型態
    RIDE_TYPE("policy-coverage", "coverageItemType"),

    // 保全受理狀態
    ACCEPTANCE_STATUS("policy-change-acceptance", "acceptance_status"),

    // 保全變更項目
    CHANGE_ITEM("policy-change-item", "change_item_code"),

    // 郵遞區號前三碼對應縣市區
    POSTAL_CODE_ZIP_CODE3("postal-code", "zip_code3"),

    // 畫面支線與角色
    SCREEN_PERMISSION("main-screen", "screen"),

    // 畫面功能代碼
    SCREEN_FUNCTION("main-screen", "function_code"),

    // 前端導覽群組與功能中文名稱
    NAVIGATION_LABEL("main-navigation", "navigation_label"),

    // 使用者授權畫面支線與角色
    USER_AUTHORIZATION("user-authorization", "role_code");

    private final String codeGroup;
    private final String codeField;

    CodeTable(String codeGroup, String codeField) {
        this.codeGroup = codeGroup;
        this.codeField = codeField;
    }
}
