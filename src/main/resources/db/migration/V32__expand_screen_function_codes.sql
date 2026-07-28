-- 將舊的 M 加三碼功能代碼升級為依業務領域分類的正式功能代碼。
UPDATE code_description
SET code_before = CASE code_before
    WHEN 'M001' THEN 'MPS00001'
    WHEN 'M002' THEN 'MPS00002'
    WHEN 'M003' THEN 'MPM00001'
    WHEN 'M004' THEN 'MPM00002'
    WHEN 'M005' THEN 'MPM00003'
    WHEN 'M006' THEN 'MPS00003'
    WHEN 'M007' THEN 'MCM00001'
    WHEN 'M008' THEN 'MUS00001'
    ELSE code_before
END
WHERE code_group = 'main-screen'
  AND code_field = 'function_code'
  AND code_before IN ('M001', 'M002', 'M003', 'M004', 'M005', 'M006', 'M007', 'M008');

-- 代碼對照表覆核原使用 M007，歷史主檔與不可覆寫稽核事件一併換成 MCM00001。
UPDATE change_review_audit
SET function_code = 'MCM00001',
    review_key = CONCAT('MCM00001', SUBSTRING(review_key, 5))
WHERE function_code = 'M007'
  AND review_key LIKE 'M007|%';

UPDATE change_review
SET function_code = 'MCM00001',
    review_key = CONCAT('MCM00001', SUBSTRING(review_key, 5))
WHERE function_code = 'M007'
  AND review_key LIKE 'M007|%';
