UPDATE code_description
SET code_description = CASE code_field
    WHEN 'CHT-code' THEN '欄位中文說明'
    WHEN 'main-policy-address' THEN '保單地址'
    WHEN 'main-policy-ride' THEN '保單主附約'
    WHEN 'policy-change-acceptance' THEN '保全受理狀態'
    WHEN 'policy-change-item' THEN '保全變更項目'
    WHEN 'postal-code' THEN '郵遞區號'
    ELSE code_description
END
WHERE code_group = 'main-code';
