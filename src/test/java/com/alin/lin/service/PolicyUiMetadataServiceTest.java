package com.alin.lin.service;

import com.alin.lin.dao.PolicyChangeDao;
import com.alin.lin.service.impl.PolicyUiMetadataServiceImpl;
import com.alin.lin.dto.UiFieldDefinition;
import com.alin.lin.entity.CodeDescription;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PolicyUiMetadataServiceTest {
    private final PolicyChangeDao dao = mock(PolicyChangeDao.class);
    private final PolicyUiMetadataService service = new PolicyUiMetadataServiceImpl(dao);

    @Test
    void shouldExposeLengthAndDescriptionFromCodeDefinition() {
        when(dao.findUiFieldDefinitions("UI-field-ride")).thenReturn(List.of(setting(
                "productCode", "text", "4", "前端欄位規格：文字，最大 4 字"
        )));

        UiFieldDefinition field = service.fields("ride").stream()
                .filter(value -> value.getKey().equals("productCode"))
                .findFirst().orElseThrow();

        assertThat(field.getMaxLength()).isEqualTo(4);
        assertThat(field.getDescription()).isEqualTo("前端欄位規格：文字，最大 4 字");
    }

    @Test
    void shouldValidateOnlyConfiguredFields() {
        when(dao.findUiFieldDefinitions("UI-field-ride")).thenReturn(List.of(
                setting("productCode", "text", "4", "前端欄位規格：文字，最大 4 字"),
                setting("premiumAmount", "number", "17,4", "前端欄位規格：數字，總位數 17，小數 4 位")
        ));

        assertThatThrownBy(() -> service.validate("ride", Map.of("productCode", "ABCDE")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("最大 4 字");
        assertThatThrownBy(() -> service.validate("ride", Map.of("premiumAmount", new BigDecimal("12345678901234.1234"))))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("小數 4 位");
        assertThatNoException().isThrownBy(() -> service.validate("ride", Map.of("productVersion", "任意長度都略過")));
    }

    private CodeDescription setting(String field, String type, String capacity, String description) {
        CodeDescription value = new CodeDescription();
        value.setCodeField(field);
        value.setCodeBefore(type);
        value.setCodeAfter(capacity);
        value.setCodeDescription(description);
        return value;
    }
}
