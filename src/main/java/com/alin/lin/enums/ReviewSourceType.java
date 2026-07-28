package com.alin.lin.enums;

/**
 * 覆核來源類型的正規名稱。
 * <p>
 * 歷史上部分表格欄位曾使用舊名（POLICY_MASTER、CODE_TABLE 等），
 * {@link #canonical(String)} 統一將舊名映射為正規名，
 * 使 ChangeReviewApplier 與 ChangeReviewServiceImpl 共用同一套判斷邏輯，
 * 不再各自維護字串比對。
 */
public enum ReviewSourceType {

    POLICY_CONTRACT,
    POLICY_CONTACT,
    POLICY_COVERAGE,
    CODE_DEFINITION,
    USER_AUTHORIZATION;

    /**
     * 將資料庫中儲存的 sourceType 字串（含舊名）映射為目前正規的 enum 值。
     * 若無法識別則回傳 {@code null}，由呼叫端決定如何處理未知類型。
     */
    public static ReviewSourceType canonical(String sourceType) {
        if (sourceType == null) return null;
        return switch (sourceType) {
            case "POLICY_MASTER", "POLICY_CONTRACT" -> POLICY_CONTRACT;
            case "POLICY_ADDRESS", "POLICY_CONTACT" -> POLICY_CONTACT;
            case "POLICY_RIDE", "POLICY_COVERAGE" -> POLICY_COVERAGE;
            case "CODE_TABLE", "CODE_DEFINITION" -> CODE_DEFINITION;
            case "USER_AUTHORIZATION" -> USER_AUTHORIZATION;
            default -> null;
        };
    }

    // --- 類型判斷的語意化捷徑，供 Service 層直接使用 ---

    public static boolean isPolicyContract(String sourceType) {
        return POLICY_CONTRACT == canonical(sourceType);
    }

    public static boolean isCodeDefinition(String sourceType) {
        return CODE_DEFINITION == canonical(sourceType);
    }

    public static boolean isPolicyContact(String sourceType) {
        return POLICY_CONTACT == canonical(sourceType);
    }

    public static boolean isPolicyCoverage(String sourceType) {
        return POLICY_COVERAGE == canonical(sourceType);
    }
}
