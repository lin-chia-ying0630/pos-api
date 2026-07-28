-- 共用表格與維護彈窗只送欄位 key；中文名稱由 CHT-code 統一管理。
INSERT INTO code_description (code_group, code_field, code_before, code_after, code_description)
SELECT * FROM (
    SELECT 'CHT-code' AS code_group, 'zipCode' AS code_field, '郵遞區號' AS code_before,
           NULL AS code_after, '郵遞區號' AS code_description UNION ALL
    SELECT 'CHT-code', 'actions', '操作', NULL, '操作'
) incoming
WHERE NOT EXISTS (
    SELECT 1 FROM code_description existing
    WHERE existing.code_group = incoming.code_group AND existing.code_field = incoming.code_field
);
