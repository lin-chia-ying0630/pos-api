-- API 路徑與畫面功能代碼的對照集中於 code table，避免 Filter 重複硬編碼。
-- code_field = HTTP method；code_before = 路徑前綴；code_after = 可存取的功能代碼（逗號分隔代表任一即可）。
INSERT INTO code_definition (
    code_definition_id,
    code_group,
    code_field,
    code_before,
    code_after,
    code_description,
    active_flag,
    review_status
)
VALUES
    (UUID(), 'api-screen-authorization', 'GET',  '/api/policy-masters',             'MPM00001,MPM00004', '查詢保單主檔 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', 'POST', '/api/policy-masters',             'MPM00004',          '新增保單主檔 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', 'PUT',  '/api/policy-masters',             'MPM00004',          '修改保單主檔 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', 'DELETE','/api/policy-masters',            'MPM00004',          '刪除保單主檔 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', 'GET',  '/api/policy-details/addresses',   'MPM00002,MPM00005', '查詢保單地址 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', 'POST', '/api/policy-details/addresses',   'MPM00005',          '新增保單地址 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', 'PUT',  '/api/policy-details/addresses',   'MPM00005',          '修改保單地址 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', 'DELETE','/api/policy-details/addresses',  'MPM00005',          '刪除保單地址 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', 'GET',  '/api/policy-details/rides',       'MPM00003,MPM00006', '查詢保單附約 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', 'POST', '/api/policy-details/rides',       'MPM00006',          '新增保單附約 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', 'PUT',  '/api/policy-details/rides',       'MPM00006',          '修改保單附約 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', 'DELETE','/api/policy-details/rides',      'MPM00006',          '刪除保單附約 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', 'GET',  '/api/policy-details/coverages',   'MPM00003,MPM00006', '查詢保單保障 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', 'POST', '/api/policy-details/coverages',   'MPM00006',          '新增保單保障 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', 'PUT',  '/api/policy-details/coverages',   'MPM00006',          '修改保單保障 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', 'DELETE','/api/policy-details/coverages',  'MPM00006',          '刪除保單保障 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', '*',    '/api/policy-ui-metadata/master',  'MPM00001,MPM00004', '保單主檔欄位定義 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', '*',    '/api/policy-ui-metadata/contract','MPM00001,MPM00004', '保單契約欄位定義 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', '*',    '/api/policy-ui-metadata/address', 'MPM00002,MPM00005', '保單地址欄位定義 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', '*',    '/api/policy-ui-metadata/contact', 'MPM00002,MPM00005', '保單聯絡欄位定義 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', '*',    '/api/policy-ui-metadata/ride',    'MPM00003,MPM00006', '保單附約欄位定義 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', '*',    '/api/policy-ui-metadata/coverage','MPM00003,MPM00006', '保單保障欄位定義 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', 'GET',  '/api/user-authorizations/codes',  'MCM00001,MCM00002', '查詢代碼對照 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', 'POST', '/api/user-authorizations/codes',  'MCM00002',          '新增代碼對照 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', 'PUT',  '/api/user-authorizations/codes',  'MCM00002',          '修改代碼對照 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', 'DELETE','/api/user-authorizations/codes', 'MCM00002',          '刪除代碼對照 API', 'Y', 'S')
AS incoming
ON DUPLICATE KEY UPDATE
    code_after = incoming.code_after,
    code_description = incoming.code_description,
    active_flag = incoming.active_flag,
    review_status = incoming.review_status;
