-- 將技術性或語意錯誤的表名改為壽險業務實體名稱。
RENAME TABLE main_policy_master TO policy_contract;
RENAME TABLE main_policy_address TO policy_contact;
RENAME TABLE main_policy_ride TO policy_coverage;
RENAME TABLE policy_change_file TO policy_change_record_snapshot;

-- policy_coverage 同時保存主約與附約，因此不可使用 rider 作共同欄位名稱。
ALTER TABLE policy_coverage
    RENAME COLUMN rider_type TO coverage_item_type,
    RENAME COLUMN rider_seq TO coverage_item_seq;

-- 正規化仍在流程中的欄位路徑；稽核 JSON 原文不覆寫，以保存當時提交內容。
UPDATE policy_change_field
SET changed_field_name = REPLACE(changed_field_name, 'main_policy_ride.', 'policy_coverage.')
WHERE changed_field_name LIKE 'main_policy_ride.%';

UPDATE policy_change_field
SET changed_field_name = REPLACE(changed_field_name, '.premium', '.premium_amount')
WHERE changed_field_name LIKE 'policy_coverage.%.premium';

UPDATE policy_change_record_snapshot
SET changed_record_type = CASE changed_record_type
    WHEN 'main_policy_master' THEN 'policy_contract'
    WHEN 'main_policy_address' THEN 'policy_contact'
    WHEN 'main_policy_ride' THEN 'policy_coverage'
    ELSE changed_record_type
END;

UPDATE code_description
SET code_group = 'policy-contact'
WHERE code_group = 'main-policy-address';

UPDATE code_description
SET code_group = 'policy-coverage'
WHERE code_group = 'main-policy-ride';

UPDATE code_description
SET code_field = 'coverageItemType'
WHERE code_group = 'CHT-code' AND code_field = 'riderType';

UPDATE code_description
SET code_field = 'coverageItemSeq'
WHERE code_group = 'CHT-code' AND code_field = 'riderSeq';

INSERT INTO code_description (code_group, code_field, code_before, code_after, code_description)
VALUES
    ('CHT-code', 'coverageItemType', 'coverageItemType', NULL, '保障項目類型'),
    ('CHT-code', 'coverageItemSeq', 'coverageItemSeq', NULL, '保障項目序號')
AS incoming
ON DUPLICATE KEY UPDATE code_description = incoming.code_description;
