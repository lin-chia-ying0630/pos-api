INSERT INTO code_definition
    (code_group, code_field, code_before, code_after, code_description, active_flag, review_status)
SELECT incoming.code_group, incoming.code_field, incoming.code_before, NULL, incoming.code_before, 'Y', 'S'
FROM (
    SELECT 'CHT-code' code_group, 'currencyCode' code_field, '幣別代碼' code_before UNION ALL
    SELECT 'CHT-code', 'productVersion', '商品版本' UNION ALL
    SELECT 'CHT-code', 'recordVersion', '資料版本號' UNION ALL
    SELECT 'CHT-code', 'workflowMode', '覆核工作模式' UNION ALL
    SELECT 'CHT-code', 'key1', '主要查詢鍵'
) incoming
WHERE NOT EXISTS (
    SELECT 1 FROM code_definition existing
    WHERE existing.code_group = incoming.code_group AND existing.code_field = incoming.code_field
);
