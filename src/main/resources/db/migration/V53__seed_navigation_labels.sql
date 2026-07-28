-- 側邊導覽的群組與功能中文名稱由代碼表管理；前端只保存穩定 key。
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
    (UUID(), 'main-navigation', 'navigation_label', 'group.change',        'change',              '保全服務',     'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'group.policy',        'policy',              '保單服務',     'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'group.code',          'code',                '代碼設定',     'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'group.review',        'review',              '覆核中心',     'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'group.authorization', 'authorization',       '使用者授權',   'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'route.change-create', 'change-create',       '新增保全變更', 'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'route.change-query',  'change-query',        '查詢保全變更', 'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'route.policy-query',  'policy-query',        '保單主檔',     'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'route.policy-address-query', 'policy-address-query', '保單地址', 'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'route.policy-ride-query', 'policy-ride-query', '保單主附約', 'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'route.code-query',    'code-query',          '代碼對照表',   'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'route.change-review-center', 'change-review-center', '覆核中心', 'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'route.change-review', 'change-review',       '保全覆核',     'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'route.user-authorization', 'user-authorization', '使用者授權', 'Y', 'S')
AS incoming
ON DUPLICATE KEY UPDATE
    code_after = incoming.code_after,
    code_description = incoming.code_description,
    active_flag = incoming.active_flag,
    review_status = incoming.review_status;

-- 使用者角色選項同樣由代碼表提供，前端不得保存固定角色中文。
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
    (UUID(), 'user-authorization', 'role_code', 'MAKER',    'ROLE_MAKER',    '經辦',     'Y', 'S'),
    (UUID(), 'user-authorization', 'role_code', 'REVIEWER', 'ROLE_REVIEWER', '覆核',     'Y', 'S'),
    (UUID(), 'user-authorization', 'role_code', 'USER',     'ROLE_USER',     '授權查詢', 'Y', 'S'),
    (UUID(), 'user-authorization', 'role_code', 'ADMIN',    'ROLE_ADMIN',    '授權管理', 'Y', 'S')
AS incoming
ON DUPLICATE KEY UPDATE
    code_after = incoming.code_after,
    code_description = incoming.code_description,
    active_flag = incoming.active_flag,
    review_status = incoming.review_status;

-- 共用清單仍缺少的欄位名稱補入 CHT-code；畫面只傳英文 key。
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
    (UUID(), 'CHT-code', 'changeCaseNo',                '案號',       NULL, '保全案件案號', 'Y', 'S'),
    (UUID(), 'CHT-code', 'changeItemCodeDescriptions',  '變更項目',   NULL, '保全變更項目中文內容', 'Y', 'S'),
    (UUID(), 'CHT-code', 'acceptanceStatus',            '狀態',       NULL, '保全受理狀態', 'Y', 'S'),
    (UUID(), 'CHT-code', 'changedFieldNames',           '異動欄位',   NULL, '異動欄位明細', 'Y', 'S'),
    (UUID(), 'CHT-code', 'changedRecordTypes',          '異動檔案',   NULL, '異動資料快照', 'Y', 'S'),
    (UUID(), 'CHT-code', 'uniqueKey',                   '唯一 Key',   NULL, '覆核資料唯一業務鍵', 'Y', 'S'),
    (UUID(), 'CHT-code', 'enabled',                     '啟用狀態',   NULL, '使用者帳號啟用狀態', 'Y', 'S'),
    (UUID(), 'CHT-code', 'roles',                       '角色',       NULL, '使用者角色集合', 'Y', 'S'),
    (UUID(), 'CHT-code', 'functionCodes',               '畫面授權',   NULL, '使用者功能代碼集合', 'Y', 'S'),
    (UUID(), 'CHT-code', 'initialPassword',             '初始密碼',   NULL, '新帳號初始密碼', 'Y', 'S'),
    (UUID(), 'CHT-code', 'newPassword',                 '新密碼',     NULL, '重設後的新密碼', 'Y', 'S'),
    (UUID(), 'CHT-code', 'passwordConfirmation',        '再次輸入新密碼', NULL, '新密碼確認', 'Y', 'S'),
    (UUID(), 'CHT-code', 'fieldName',                   '欄位',       NULL, '資料欄位名稱', 'Y', 'S'),
    (UUID(), 'CHT-code', 'contentBefore',               '異動前',     NULL, '異動前內容', 'Y', 'S'),
    (UUID(), 'CHT-code', 'contentAfter',                '異動後',     NULL, '異動後內容', 'Y', 'S'),
    (UUID(), 'CHT-code', 'reviewDetail',                '資料詳細內容', NULL, '覆核異動前後明細', 'Y', 'S'),
    (UUID(), 'CHT-code', 'operation',                   '操作',       NULL, '畫面操作', 'Y', 'S')
AS incoming
ON DUPLICATE KEY UPDATE
    code_after = incoming.code_after,
    code_description = incoming.code_description,
    active_flag = incoming.active_flag,
    review_status = incoming.review_status;
