package com.alin.lin.dao;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/** 防止把 Java 欄位名稱誤組成不存在的資料表名稱。 */
class MapperSchemaContractTest {
    @Test
    void usesCanonicalPolicyChangeTableNames() throws Exception {
        try (var input = getClass().getResourceAsStream("/mapper/PolicyChangeDao.xml")) {
            assertThat(input).isNotNull();
            String xml = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(xml)
                    .doesNotContain("policy_change_item_code")
                    .doesNotContain("policy_changed_field_name")
                    .doesNotContain("policy_changed_record_type")
                    .doesNotContain("#{premium_amount}")
                    .contains("policy_change_item")
                    .contains("policy_change_field")
                    .contains("policy_change_record_snapshot");
        }
    }
}
