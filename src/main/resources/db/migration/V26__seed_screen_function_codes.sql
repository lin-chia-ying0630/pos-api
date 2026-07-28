-- 畫面功能代碼集中由 code_description 管理；第一碼固定為 M。
INSERT IGNORE INTO code_description
    (code_group, code_field, code_before, code_after, code_description, active_flag)
VALUES
    ('main-screen', 'function_code', 'M001', 'CREATE', '新增保全變更', 'Y'),
    ('main-screen', 'function_code', 'M002', 'QUERY_CHANGE', '查詢保全變更', 'Y'),
    ('main-screen', 'function_code', 'M003', 'QUERY_MASTER', '查詢保單主檔', 'Y'),
    ('main-screen', 'function_code', 'M004', 'QUERY_ADDRESS', '查詢保單地址', 'Y'),
    ('main-screen', 'function_code', 'M005', 'QUERY_RIDE', '查詢保單主附約', 'Y'),
    ('main-screen', 'function_code', 'M006', 'REVIEW', '保全變更覆核', 'Y'),
    ('main-screen', 'function_code', 'M007', 'CODE_TABLE', '代碼對照表', 'Y'),
    ('main-screen', 'function_code', 'M008', 'USER_AUTHORIZATION', '使用者授權', 'Y')
