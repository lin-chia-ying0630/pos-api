DELETE FROM code_description
WHERE code_group = 'main-code'
  AND code_field IN ('user-authorization', 'screen-permission', 'role');

INSERT INTO code_description
(
    code_group,
    code_field,
    code_before,
    code_after,
    code_description
)
SELECT
    'main-code',
    source.code_group,
    '1',
    '',
    CASE source.code_group
        WHEN 'CHT-code' THEN '中文對照欄位'
        ELSE CONCAT(source.code_group, '代碼')
    END
FROM (
    SELECT DISTINCT code_group
    FROM code_description
    WHERE code_group NOT IN ('main-code', 'user-authorization', 'screen-permission', 'role')
) source
ON DUPLICATE KEY UPDATE
    code_after = VALUES(code_after),
    code_description = VALUES(code_description);
