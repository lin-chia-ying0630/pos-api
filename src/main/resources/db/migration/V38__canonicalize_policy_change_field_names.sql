-- 保全作業欄位改為可辨識「代碼／欄位名稱／紀錄鍵／紀錄類型」的 canonical name。
ALTER TABLE policy_change_item
    RENAME COLUMN change_item TO change_item_code;

ALTER TABLE policy_change_field
    RENAME COLUMN change_item TO change_item_code,
    RENAME COLUMN change_field TO changed_field_name,
    RENAME COLUMN change_key TO changed_record_key;

ALTER TABLE policy_change_file
    RENAME COLUMN change_item TO change_item_code,
    RENAME COLUMN change_file TO changed_record_type,
    RENAME COLUMN change_key TO changed_record_key;

ALTER TABLE policy_change_case_reservation_item
    RENAME COLUMN change_item TO change_item_code;

UPDATE code_description
SET code_field = 'change_item_code'
WHERE code_group = 'policy-change-item' AND code_field = 'change_item';

UPDATE code_description
SET code_field = 'changeItemCode'
WHERE code_group = 'CHT-code' AND code_field = 'changeItem';

INSERT INTO code_description (code_group, code_field, code_before, code_after, code_description)
VALUES
    ('CHT-code', 'changeItemCode', 'changeItemCode', NULL, '保全項目代碼'),
    ('CHT-code', 'changedFieldName', 'changedFieldName', NULL, '變更欄位名稱'),
    ('CHT-code', 'changedRecordKey', 'changedRecordKey', NULL, '變更資料業務鍵'),
    ('CHT-code', 'changedRecordType', 'changedRecordType', NULL, '變更資料類型')
AS incoming
ON DUPLICATE KEY UPDATE code_description = incoming.code_description;
