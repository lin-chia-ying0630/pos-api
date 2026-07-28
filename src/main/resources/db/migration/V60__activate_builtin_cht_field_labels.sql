-- CHT-code 是已隨程式版本部署的共用中文欄位 DD，不屬於待人工覆核的業務異動。
-- 舊版種子資料曾保留 P 狀態，會讓 API 正確排除後找不到中文而退回英文 key。
UPDATE code_definition
SET review_status = 'S',
    active_flag = 'Y',
    reviewed_by = COALESCE(reviewed_by, 'system'),
    reviewed_at = COALESCE(reviewed_at, CURRENT_TIMESTAMP),
    updated_by = COALESCE(updated_by, 'system'),
    updated_at = CURRENT_TIMESTAMP
WHERE code_group = 'CHT-code'
  AND review_status <> 'S';
