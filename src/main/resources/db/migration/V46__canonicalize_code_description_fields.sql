-- V37 將正式地址欄位改為 address_type_code，V39 只遷移了 code_group；
-- 同步修正代碼對照的 code_field，否則既有「通訊地址 01」無法被新 enum 查到。
UPDATE code_definition
SET code_field = 'address_type_code',
    updated_by = COALESCE(updated_by, 'system'),
    updated_at = CURRENT_TIMESTAMP
WHERE code_group = 'policy-contact'
  AND code_field = 'address_type';
