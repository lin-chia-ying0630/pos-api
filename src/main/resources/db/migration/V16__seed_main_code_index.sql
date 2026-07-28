INSERT INTO code_description (code_group, code_field, code_before, code_after, code_description)
SELECT 'main-code', source.code_group,
       LPAD(ROW_NUMBER() OVER (ORDER BY source.code_group), 3, '0'),
       NULL,
       CONCAT('代碼群組：', source.code_group)
FROM (
    SELECT DISTINCT code_group
    FROM code_description
    WHERE code_group NOT IN ('main-code', 'user-authorization', 'screen-permission', 'role')
) source
ON DUPLICATE KEY UPDATE
    code_description = VALUES(code_description);
