package com.alin.lin.dao;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/** 擴檔必須同步父子表與前端 metadata，避免部署後出現 FK 型別或畫面長度落差。 */
class LifeInsuranceCapacityMigrationTest {
    @Test
    void expandsAllPolicyForeignKeyColumnsAndUiMetadataTogether() throws Exception {
        try (var input = getClass().getResourceAsStream(
                "/db/migration/V49__expand_life_insurance_field_capacity.sql")) {
            assertThat(input).isNotNull();
            String sql = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(sql)
                    .contains("ALTER TABLE policy_contract")
                    .contains("ALTER TABLE policy_contact")
                    .contains("ALTER TABLE policy_coverage")
                    .contains("ALTER TABLE policy_change_acceptance")
                    .contains("ALTER TABLE policy_change_item")
                    .contains("ALTER TABLE policy_change_field")
                    .contains("ALTER TABLE policy_change_record_snapshot")
                    .contains("ALTER TABLE policy_change_case_reservation")
                    .contains("ALTER TABLE change_review")
                    .contains("policy_no VARCHAR(20)")
                    .contains("insured_amount DECIMAL(18, 2)")
                    .contains("premium_amount DECIMAL(18, 4)")
                    .contains("UPDATE code_definition");
        }
    }

    @Test
    void expandsCoreFilesWithCanonicalLifeInsuranceFields() throws Exception {
        try (var input = getClass().getResourceAsStream(
                "/db/migration/V50__expand_core_life_insurance_file_fields.sql")) {
            assertThat(input).isNotNull();
            String sql = new String(input.readAllBytes(), StandardCharsets.UTF_8);

            assertThat(sql)
                    .contains("policy_status VARCHAR(2)")
                    .contains("contract_date DATE")
                    .contains("application_no VARCHAR(32)")
                    .contains("customer_code VARCHAR(32)")
                    .contains("insurance_agent_code VARCHAR(32)")
                    .contains("postal_code VARCHAR(6)")
                    .contains("email_address VARCHAR(254)")
                    .contains("telephone_no VARCHAR(30)")
                    .contains("mobile_no VARCHAR(30)")
                    .contains("product_name VARCHAR(200)")
                    .contains("payment_frequency_code VARCHAR(4)")
                    .contains("premium_payment_term_years INT")
                    .contains("UPDATE policy_contact")
                    .contains("INSERT INTO code_definition");
        }
    }

    @Test
    void normalizesContactChannelsAndAddsCoreUuidIdentifiers() throws Exception {
        String contactSql;
        try (var input = getClass().getResourceAsStream(
                "/db/migration/V51__normalize_policy_contact_channels.sql")) {
            assertThat(input).isNotNull();
            contactSql = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        String idSql;
        try (var input = getClass().getResourceAsStream(
                "/db/migration/V52__add_core_uuid_identifiers.sql")) {
            assertThat(input).isNotNull();
            idSql = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }

        assertThat(contactSql)
                .contains("CREATE TABLE policy_contact_address")
                .contains("CREATE TABLE policy_contact_email")
                .contains("CREATE TABLE policy_contact_phone")
                .contains("address_id CHAR(36)")
                .contains("email_id CHAR(36)")
                .contains("phone_id CHAR(36)");
        assertThat(idSql)
                .contains("policy_contract_id CHAR(36)")
                .contains("coverage_id CHAR(36)")
                .contains("change_case_id CHAR(36)")
                .contains("change_item_id CHAR(36)")
                .contains("change_field_id CHAR(36)")
                .contains("change_snapshot_id CHAR(36)")
                .contains("review_uuid CHAR(36)");
    }
}
