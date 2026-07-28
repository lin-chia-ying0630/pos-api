-- 系統內建的地址類型中文欄位已完成部署，不應因環境資料漂移而持續顯示待覆核。
UPDATE code_definition
SET review_status = 'S',
    updated_by = 'system',
    updated_at = CURRENT_TIMESTAMP
WHERE code_group = 'CHT-code'
  AND code_field = 'addressTypeCode'
  AND code_before = '地址類型';
