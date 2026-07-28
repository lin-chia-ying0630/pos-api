package com.alin.lin.util;

public final class ValidationPatterns {
    public static final String POLICY_NO = "^[A-Z0-9]{1,20}$";
    public static final String CHANGE_CASE_NO = "^C\\d{10,}$";
    public static final String POSTAL_CODE = "^\\d{3}(?:\\d{3})?$";
    public static final String ZIP_CODE_PART = "^$|^\\d{3}$";
    public static final String ADDRESS_TYPE = "^[A-Z0-9]{1,8}$";
    public static final String RIDE_ORDER = "^\\d{1,10}$";
    public static final String CHANGE_ITEM = "^\\d{3}$";

    private ValidationPatterns() {
    }
}
