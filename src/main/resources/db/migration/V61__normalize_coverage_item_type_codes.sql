-- policy_coverage 的主附約類型統一使用清楚的 BASE/RIDER，
-- 避免資料列、DTO 驗證與代碼對照分別使用 1/2/3 三套語意。
UPDATE policy_coverage
SET coverage_item_type = CASE
    WHEN coverage_item_type = '1' THEN 'BASE'
    WHEN coverage_item_type IN ('2', '3') THEN 'RIDER'
    ELSE coverage_item_type
END
WHERE coverage_item_type IN ('1', '2', '3');

DELETE FROM code_definition
WHERE code_group = 'policy-coverage'
  AND code_field = 'coverageItemType'
  AND code_before IN ('1', '2', '3');

INSERT INTO code_definition
    (code_definition_id, code_group, code_field, code_before, code_after, code_description, active_flag, review_status)
VALUES
    (UUID(), 'policy-coverage', 'coverageItemType', 'BASE', NULL, '主約', 'Y', 'S'),
    (UUID(), 'policy-coverage', 'coverageItemType', 'RIDER', NULL, '附約', 'Y', 'S')
AS incoming
ON DUPLICATE KEY UPDATE
    code_description = incoming.code_description,
    active_flag = incoming.active_flag,
    review_status = incoming.review_status;
