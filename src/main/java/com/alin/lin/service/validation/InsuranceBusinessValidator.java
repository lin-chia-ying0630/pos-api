package com.alin.lin.service.validation;

import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.entity.PolicyContact;
import com.alin.lin.entity.PolicyContract;
import com.alin.lin.entity.PolicyCoverage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * 壽險資料的跨欄位規則集中於後端；前端檢核只提供操作提示，不能取代本類。
 */
@Component
public class InsuranceBusinessValidator {
    private final PolicyChangeDao dao;

    public InsuranceBusinessValidator(PolicyChangeDao dao) {
        this.dao = dao;
    }

    public void validateContract(PolicyContract value) {
        if (value == null || isBlank(value.getPolicyNo()) || value.getPolicySeq() == null
                || value.getPolicySeq() <= 0 || value.getPremiumAmount() == null
                || value.getPremiumAmount().signum() < 0 || isBlank(value.getCurrencyCode())) {
            throw new IllegalArgumentException("保單號碼、序號、保費及幣別不可空白");
        }
        value.setCurrencyCode(value.getCurrencyCode().trim().toUpperCase(Locale.ROOT));
        if (!value.getCurrencyCode().matches("[A-Z]{3}")) {
            throw new IllegalArgumentException("幣別必須是 3 碼 ISO 4217 代碼");
        }
        if (value.getEffectiveDate() != null && value.getMaturityDate() != null
                && value.getMaturityDate().isBefore(value.getEffectiveDate())) {
            throw new IllegalArgumentException("保單滿期日不可早於生效日");
        }
        requirePositiveWhenPresent(value.getPremiumPaymentTermYears(), "繳費年期");
        requirePositiveWhenPresent(value.getCoverageTermYears(), "保險期間年期");
        if (value.getPremiumPaymentTermYears() != null && value.getCoverageTermYears() != null
                && value.getPremiumPaymentTermYears() > value.getCoverageTermYears()) {
            throw new IllegalArgumentException("繳費年期不可超過保險期間年期");
        }
    }

    public void validateContact(PolicyContact value) {
        if (value == null || isBlank(value.getPolicyNo()) || value.getPolicySeq() == null
                || value.getPolicySeq() <= 0 || isBlank(value.getAddressTypeCode())) {
            throw new IllegalArgumentException("保單號碼、保單序號與地址類型不可空白");
        }
        if (dao.findMaster(value.getPolicyNo(), value.getPolicySeq()) == null) {
            throw new IllegalArgumentException("保單不存在，無法維護聯絡資料");
        }
        if (length(value.getPolicyNo()) > 20 || length(value.getAddressTypeCode()) > 8) {
            throw new IllegalArgumentException("保單號碼最多 20 碼，地址類型代碼最多 8 碼");
        }
        if (isBlank(value.getPostalCode()) || !value.getPostalCode().matches("^\\d{3}(?:\\d{3})?$")
                || isBlank(value.getAddressText()) || length(value.getAddressText()) > 300) {
            throw new IllegalArgumentException("地址必須包含 3 或 6 碼郵遞區號及地址內容");
        }
    }

    public void validateCoverage(PolicyCoverage value, boolean creating) {
        if (value == null || isBlank(value.getPolicyNo()) || value.getPolicySeq() == null
                || value.getPolicySeq() <= 0 || isBlank(value.getCoverageItemSeq())
                || isBlank(value.getCoverageItemType()) || isBlank(value.getProductCode())) {
            throw new IllegalArgumentException("保單、保障項目類型、序號及商品代碼不可空白");
        }
        var contract = dao.findMaster(value.getPolicyNo(), value.getPolicySeq());
        if (contract == null) {
            throw new IllegalArgumentException("保單不存在，無法維護保障項目");
        }
        String type = value.getCoverageItemType().trim().toUpperCase(Locale.ROOT);
        if (!"BASE".equals(type) && !"RIDER".equals(type)) {
            throw new IllegalArgumentException("保障項目類型只能是 BASE 或 RIDER");
        }
        value.setCoverageItemType(type);
        if (length(value.getPolicyNo()) > 20 || !value.getCoverageItemSeq().matches("\\d{1,10}")) {
            throw new IllegalArgumentException("保單號碼最多 20 碼，保障項目序號必須為 1 至 10 碼數字");
        }
        if (length(value.getProductCode()) > 32 || length(value.getProductVersion()) > 32) {
            throw new IllegalArgumentException("商品代碼及商品版本最多 32 碼");
        }
        value.setCurrencyCode(value.getCurrencyCode().trim().toUpperCase(Locale.ROOT));
        if (!value.getCurrencyCode().matches("[A-Z]{3}")) throw new IllegalArgumentException("幣別必須是 3 碼 ISO 4217 代碼");
        if (!value.getCurrencyCode().equalsIgnoreCase(contract.getCurrencyCode())) {
            throw new IllegalArgumentException("保障項目幣別必須與保單契約一致");
        }
        if (value.getCoverageTermYears() == null || value.getCoverageTermYears() <= 0) {
            throw new IllegalArgumentException("保障期間必須大於 0");
        }
        requireNonNegative(value.getInsuredAmount(), "保額");
        requireNonNegative(value.getPremiumAmount(), "保費");

        var existing = dao.findRides(value.getPolicyNo(), value.getPolicySeq());
        if ("BASE".equals(type) && existing.stream().anyMatch(item ->
                "BASE".equalsIgnoreCase(item.getCoverageItemType())
                        && (creating || !item.getCoverageItemSeq().equals(value.getCoverageItemSeq())))) {
            throw new IllegalArgumentException("每張保單只能有一筆 BASE 主約");
        }
        if ("RIDER".equals(type)) {
            PolicyCoverage base = existing.stream()
                    .filter(item -> "BASE".equalsIgnoreCase(item.getCoverageItemType()))
                    .filter(item -> !item.getCoverageItemSeq().equals(value.getCoverageItemSeq()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("附約必須依附有效主約"));
        if (base.getCoverageTermYears() != null && value.getCoverageTermYears() > base.getCoverageTermYears()) {
                throw new IllegalArgumentException("附約保障期間不可超過主約");
            }
        }
        requirePositiveWhenPresent(value.getPremiumPaymentTermYears(), "繳費年期");
        if (value.getPremiumPaymentTermYears() != null
                && value.getPremiumPaymentTermYears() > value.getCoverageTermYears()) {
            throw new IllegalArgumentException("繳費年期不可超過保障期間");
        }
        if (value.getEffectiveDate() != null && value.getExpiryDate() != null
                && value.getExpiryDate().isBefore(value.getEffectiveDate())) {
            throw new IllegalArgumentException("保障終止日不可早於生效日");
        }
    }

    private void requireNonNegative(BigDecimal value, String field) {
        if (value == null || value.signum() < 0) throw new IllegalArgumentException(field + "不可空白或小於 0");
    }
    private void requirePositiveWhenPresent(Integer value, String field) {
        if (value != null && value <= 0) throw new IllegalArgumentException(field + "必須大於 0");
    }
    private int length(String value) { return value == null ? 0 : value.length(); }
    private boolean isBlank(String value) { return value == null || value.isBlank(); }
}
