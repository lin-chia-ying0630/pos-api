package com.alin.lin.service.impl;

import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.dto.UiFieldDefinition;
import com.alin.lin.entity.CodeDescription;
import com.alin.lin.service.PolicyUiMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 從 main.code_definition 取得前端表單使用的資料型別、長度與說明。
 * 必填、識別鍵及編輯權限仍由後端流程固定，避免一般代碼維護誤改安全行為。
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PolicyUiMetadataServiceImpl implements PolicyUiMetadataService {
    private final PolicyChangeDao dao;

    @Override
    public List<UiFieldDefinition> fields(String entity) {
        List<UiFieldDefinition> structure = switch (entity) {
            case "master", "contract" -> masterFields();
            case "address", "contact" -> addressFields();
            case "ride", "coverage" -> rideFields();
            default -> throw new IllegalArgumentException("不支援的保單資料類型: " + entity);
        };
        String group = switch (entity) {
            case "master", "contract" -> "UI-field-master";
            case "address", "contact" -> "UI-field-address";
            default -> "UI-field-ride";
        };
        Map<String, CodeDescription> settings = dao.findUiFieldDefinitions(group).stream()
                .collect(Collectors.toMap(CodeDescription::getCodeField, Function.identity(), (first, ignored) -> first));
        return structure.stream().map(field -> merge(field, settings.get(field.getKey()))).toList();
    }

    /** 有 code_definition 設定才檢查；未設定的欄位直接略過。 */
    @Override
    public void validate(String entity, Map<String, ?> values) {
        for (UiFieldDefinition field : fields(entity)) {
            Object value = values.get(field.getKey());
            if (field.getDescription() == null || value == null) continue;
            if (field.getMaxLength() != null && String.valueOf(value).length() > field.getMaxLength()) invalid(field);
            if ("number".equals(field.getType()) && field.getPrecision() != null && field.getScale() != null) {
                java.math.BigDecimal number;
                try {
                    number = new java.math.BigDecimal(String.valueOf(value));
                } catch (NumberFormatException ex) {
                    invalid(field);
                    return;
                }
                int scale = Math.max(number.scale(), 0);
                int integerDigits = Math.max(number.precision() - scale, 0);
                if (scale > field.getScale() || integerDigits > field.getPrecision() - field.getScale()) invalid(field);
            }
            if (field.getOptions() != null && !field.getOptions().contains(String.valueOf(value))) invalid(field);
        }
    }

    private void invalid(UiFieldDefinition field) {
        throw new IllegalArgumentException(field.getKey() + " 不符合設定；" + field.getDescription());
    }

    private UiFieldDefinition merge(UiFieldDefinition field, CodeDescription setting) {
        if (setting == null) {
            // code_definition 採 opt-in：沒有建立設定就不套用長度／精度檢查，也不阻斷 API。
            return field;
        }
        String defaultType = field.getType();
        try {
            field.setType(setting.getCodeBefore());
            field.setDescription(setting.getCodeDescription());
            String capacity = setting.getCodeAfter();
            if ("number".equals(field.getType()) && capacity != null) {
                String[] parts = capacity.split(",", -1);
                if (parts.length != 2) throw new NumberFormatException("數字規格必須是 precision,scale");
                field.setPrecision(Integer.valueOf(parts[0]));
                field.setScale(Integer.valueOf(parts[1]));
                field.setMaxLength(null);
            } else if (capacity != null && !capacity.isBlank()) {
                field.setMaxLength(Integer.valueOf(capacity));
                field.setPrecision(null);
                field.setScale(null);
            }
        } catch (RuntimeException ex) {
            log.warn("忽略不合法的 UI 欄位設定 group={}, field={}", setting.getCodeGroup(), field.getKey());
            field.setType(defaultType);
            field.setMaxLength(null);
            field.setPrecision(null);
            field.setScale(null);
            field.setDescription(null);
        }
        return field;
    }

    private List<UiFieldDefinition> masterFields() {
        return List.of(f("policyNo", true, true, true, false), number("policySeq", true, true, "1", "1"),
                number("premiumAmount", true, false, "0.0001", "0"), f("currencyCode", true, false, true, false),
                f("policyStatus", false, false, true, false), date("contractDate"), date("effectiveDate"),
                date("maturityDate"), optionalNumber("premiumPaymentTermYears"), optionalNumber("coverageTermYears"),
                f("coverageTermType", false, false, true, false),
                f("paymentFrequencyCode", false, false, true, false),
                f("productCode", false, false, true, false), f("productVersion", false, false, true, false),
                f("productName", false, false, true, true), f("basePlanProductCode", false, false, true, false),
                f("applicationNo", false, false, true, false), f("customerCode", false, false, true, false),
                f("insuranceAgentCode", false, false, true, false),
                status("activeFlag", List.of("Y", "N")), status("reviewStatus", List.of("P", "S", "C")),
                audit("recordVersion"), audit("createdBy"), audit("createdAt"), audit("updatedBy"), audit("updatedAt"),
                audit("reviewedBy"), audit("reviewedAt"));
    }

    private List<UiFieldDefinition> addressFields() {
        return List.of(f("policyNo", true, true, true, false), number("policySeq", true, true, "1", "1"),
                f("addressId", false, true, false, false),
                f("addressTypeCode", true, true, true, false),
                f("postalCode", true, false, true, false),
                f("addressText", true, false, true, true),
                f("countryCode", true, false, true, false),
                f("primaryFlag", true, false, true, false),
                status("activeFlag", List.of("Y", "N")),
                status("reviewStatus", List.of("P", "S", "C")), audit("recordVersion"), audit("createdBy"),
                audit("createdAt"), audit("updatedBy"), audit("updatedAt"), audit("reviewedBy"), audit("reviewedAt"));
    }

    private List<UiFieldDefinition> rideFields() {
        return List.of(f("policyNo", true, true, true, false), number("policySeq", true, true, "1", "1"),
                f("coverageItemSeq", true, true, true, false), choice("coverageItemType", List.of("BASE", "RIDER")),
                f("productCode", true, false, true, false), f("productVersion", true, false, true, false),
                number("coverageTermYears", true, false, "1", "1"), number("insuredAmount", true, false, "0.01", "0"),
                number("premiumAmount", true, false, "0.0001", "0"), f("currencyCode", true, false, true, false),
                f("productName", false, false, true, true), f("basePlanProductCode", false, false, true, false),
                f("paymentFrequencyCode", false, false, true, false), optionalNumber("premiumPaymentTermYears"),
                f("coverageTermType", false, false, true, false), date("effectiveDate"), date("expiryDate"),
                status("activeFlag", List.of("Y", "N")), status("reviewStatus", List.of("P", "S", "C")),
                audit("recordVersion"), audit("createdBy"), audit("createdAt"), audit("updatedBy"), audit("updatedAt"),
                audit("reviewedBy"), audit("reviewedAt"));
    }

    private UiFieldDefinition f(String key, boolean required, boolean identity, boolean editable, boolean wide) {
        return new UiFieldDefinition(key, "text", required, identity, editable, wide,
                null, null, null, null, null, null, null);
    }

    private UiFieldDefinition number(String key, boolean required, boolean identity, String step, String min) {
        return new UiFieldDefinition(key, "number", required, identity, true, false,
                null, null, null, step, min, null, null);
    }

    private UiFieldDefinition optionalNumber(String key) {
        return number(key, false, false, "1", "1");
    }

    private UiFieldDefinition date(String key) {
        return new UiFieldDefinition(key, "date", false, false, true, false,
                null, null, null, null, null, null, null);
    }

    private UiFieldDefinition audit(String key) {
        return new UiFieldDefinition(key, key.endsWith("At") ? "datetime" : "text", false, false, false, false,
                null, null, null, null, null, null, null);
    }

    private UiFieldDefinition status(String key, List<String> options) {
        return new UiFieldDefinition(key, "select", false, false, false, false,
                null, null, null, null, null, options, null);
    }

    private UiFieldDefinition choice(String key, List<String> options) {
        return new UiFieldDefinition(key, "select", true, false, true, false,
                null, null, null, null, null, options, null);
    }
}
