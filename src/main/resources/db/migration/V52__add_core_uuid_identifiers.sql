-- 核心壽險與保全資料加入穩定技術 ID；業務複合鍵繼續保留唯一約束。
ALTER TABLE policy_contract ADD COLUMN policy_contract_id CHAR(36) NULL FIRST;
UPDATE policy_contract SET policy_contract_id = UUID() WHERE policy_contract_id IS NULL;
ALTER TABLE policy_contract
    MODIFY policy_contract_id CHAR(36) NOT NULL,
    ADD UNIQUE KEY uk_policy_contract_id (policy_contract_id);

ALTER TABLE policy_coverage ADD COLUMN coverage_id CHAR(36) NULL FIRST;
UPDATE policy_coverage SET coverage_id = UUID() WHERE coverage_id IS NULL;
ALTER TABLE policy_coverage
    MODIFY coverage_id CHAR(36) NOT NULL,
    ADD UNIQUE KEY uk_policy_coverage_id (coverage_id);

ALTER TABLE policy_change_acceptance ADD COLUMN change_case_id CHAR(36) NULL FIRST;
UPDATE policy_change_acceptance SET change_case_id = UUID() WHERE change_case_id IS NULL;
ALTER TABLE policy_change_acceptance
    MODIFY change_case_id CHAR(36) NOT NULL,
    ADD UNIQUE KEY uk_policy_change_case_id (change_case_id);

ALTER TABLE policy_change_item ADD COLUMN change_item_id CHAR(36) NULL FIRST;
UPDATE policy_change_item SET change_item_id = UUID() WHERE change_item_id IS NULL;
ALTER TABLE policy_change_item
    MODIFY change_item_id CHAR(36) NOT NULL,
    ADD UNIQUE KEY uk_policy_change_item_id (change_item_id);

ALTER TABLE policy_change_field ADD COLUMN change_field_id CHAR(36) NULL AFTER id;
UPDATE policy_change_field SET change_field_id = UUID() WHERE change_field_id IS NULL;
ALTER TABLE policy_change_field
    MODIFY change_field_id CHAR(36) NOT NULL,
    ADD UNIQUE KEY uk_policy_change_field_id (change_field_id);

ALTER TABLE policy_change_record_snapshot ADD COLUMN change_snapshot_id CHAR(36) NULL AFTER id;
UPDATE policy_change_record_snapshot SET change_snapshot_id = UUID() WHERE change_snapshot_id IS NULL;
ALTER TABLE policy_change_record_snapshot
    MODIFY change_snapshot_id CHAR(36) NOT NULL,
    ADD UNIQUE KEY uk_policy_change_snapshot_id (change_snapshot_id);

ALTER TABLE change_review ADD COLUMN review_uuid CHAR(36) NULL AFTER id;
UPDATE change_review SET review_uuid = UUID() WHERE review_uuid IS NULL;
ALTER TABLE change_review
    MODIFY review_uuid CHAR(36) NOT NULL,
    ADD UNIQUE KEY uk_change_review_uuid (review_uuid);
ALTER TABLE change_review MODIFY source_record_id CHAR(36) NULL;

ALTER TABLE code_definition ADD COLUMN code_definition_id CHAR(36) NULL FIRST;
UPDATE code_definition SET code_definition_id = UUID() WHERE code_definition_id IS NULL;
ALTER TABLE code_definition
    MODIFY code_definition_id CHAR(36) NOT NULL,
    ADD UNIQUE KEY uk_code_definition_id (code_definition_id);

ALTER TABLE user_account ADD COLUMN user_account_uuid CHAR(36) NULL FIRST;
UPDATE user_account SET user_account_uuid = UUID() WHERE user_account_uuid IS NULL;
ALTER TABLE user_account
    MODIFY user_account_uuid CHAR(36) NOT NULL,
    ADD UNIQUE KEY uk_user_account_uuid (user_account_uuid);

ALTER TABLE user_role_assignment ADD COLUMN user_role_assignment_id CHAR(36) NULL FIRST;
UPDATE user_role_assignment SET user_role_assignment_id = UUID() WHERE user_role_assignment_id IS NULL;
ALTER TABLE user_role_assignment
    MODIFY user_role_assignment_id CHAR(36) NOT NULL,
    ADD UNIQUE KEY uk_user_role_assignment_id (user_role_assignment_id);

ALTER TABLE user_screen_authorization ADD COLUMN user_screen_authorization_id CHAR(36) NULL FIRST;
UPDATE user_screen_authorization
SET user_screen_authorization_id = UUID()
WHERE user_screen_authorization_id IS NULL;
ALTER TABLE user_screen_authorization
    MODIFY user_screen_authorization_id CHAR(36) NOT NULL,
    ADD UNIQUE KEY uk_user_screen_authorization_id (user_screen_authorization_id);
