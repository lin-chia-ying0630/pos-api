-- 補齊其餘業務 API 對照；動態區段使用 Ant path pattern。
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
    (UUID(), 'api-screen-authorization', '*',     '/api/change-reviews',                    'MPS00003',          '覆核中心 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', 'PATCH', '/api/change-cases/*/status',             'MPS00003',          '保全覆核決策 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', 'GET',   '/api/change-cases',                      'MPS00002',          '查詢保全案件 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', '*',     '/api/change-cases',                      'MPS00001',          '申請保全案件 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', '*',     '/api/postal-codes',                      'MPS00001',          '申請保全郵遞區號 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', '*',     '/api/policies/*/*/change-items/**',      'MPS00001',          '保全異動項目 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', '*',     '/api/policies/*/change-cases/**',        'MPS00002,MPS00003', '保全案件查詢與覆核 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', '*',     '/api/policies/*/*/change-cases/**',      'MPS00002,MPS00003', '保全案件明細與覆核 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', '*',     '/api/user-authorizations',               'MUS00001',          '使用者授權 API', 'Y', 'S'),
    (UUID(), 'api-screen-authorization', '*',     '/api/policies',                          'MPS00001,MPM00001,MPM00002,MPM00003,MPM00004,MPM00005,MPM00006', '保單共用總覽 API', 'Y', 'S')
AS incoming
ON DUPLICATE KEY UPDATE
    code_after = incoming.code_after,
    code_description = incoming.code_description,
    active_flag = incoming.active_flag,
    review_status = incoming.review_status;
