package com.alin.lin.enums;

import lombok.Getter;

@Getter
public enum CodeDescriptionMeaning {
    // 通訊地址
    COMMUNICATION_ADDRESS(CodeTable.ADDRESS_TYPE, "01"),

    // 戶籍地址
    REGISTERED_ADDRESS(CodeTable.ADDRESS_TYPE, "02"),

    // email
    EMAIL_ADDRESS(CodeTable.ADDRESS_TYPE, "31"),

    // 地址變更
    ADDRESS_CHANGE(CodeTable.CHANGE_ITEM, "001"),

    // 主約保額變更
    MAIN_AMOUNT_CHANGE(CodeTable.CHANGE_ITEM, "002"),

    // 附約保額變更
    RIDER_AMOUNT_CHANGE(CodeTable.CHANGE_ITEM, "003"),

    EMAIL_CHANGE(CodeTable.CHANGE_ITEM, "004"),

    TELEPHONE_CHANGE(CodeTable.CHANGE_ITEM, "005"),

    MOBILE_CHANGE(CodeTable.CHANGE_ITEM, "006"),

    // 受理中
    PENDING_STATUS(CodeTable.ACCEPTANCE_STATUS, "P"),

    // 覆核套用中
    PROCESSING_STATUS(CodeTable.ACCEPTANCE_STATUS, "A"),

    // 完成
    COMPLETE_STATUS(CodeTable.ACCEPTANCE_STATUS, "S"),

    // 取消
    CANCEL_STATUS(CodeTable.ACCEPTANCE_STATUS, "C"),

    // 主約
    // policy_coverage 已正規化為 BASE/RIDER，不再沿用舊主附約數字碼。
    MAIN_RIDE_TYPE(CodeTable.RIDE_TYPE, "BASE");

    private final CodeTable codeTable;
    private final String codeBefore;

    CodeDescriptionMeaning(CodeTable codeTable, String codeBefore) {
        this.codeTable = codeTable;
        this.codeBefore = codeBefore;
    }
}
