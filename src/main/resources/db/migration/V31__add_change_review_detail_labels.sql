INSERT INTO code_description (code_group, code_field, code_before, code_after, code_description)
VALUES
    ('CHT-code', 'codeGroup', '代碼群組', NULL, '代碼群組'),
    ('CHT-code', 'codeField', '代碼欄位', NULL, '代碼欄位'),
    ('CHT-code', 'codeBefore', '轉換前代碼', NULL, '轉換前代碼'),
    ('CHT-code', 'codeAfter', '轉換後代碼', NULL, '轉換後代碼'),
    ('CHT-code', 'codeDescription', '中文說明', NULL, '中文說明'),
    ('CHT-code', 'activeFlag', '啟用狀態', NULL, '啟用狀態'),
    ('CHT-code', 'reviewStatus', '覆核狀態', NULL, '覆核狀態'),
    ('CHT-code', 'createdBy', '建立人員', NULL, '建立人員'),
    ('CHT-code', 'updatedBy', '更新人員', NULL, '更新人員'),
    ('CHT-code', 'reviewedBy', '覆核人員', NULL, '覆核人員'),
    ('CHT-code', 'reviewedAt', '覆核時間', NULL, '覆核時間') AS incoming
ON DUPLICATE KEY UPDATE
    code_after = incoming.code_after,
    code_description = incoming.code_description;
