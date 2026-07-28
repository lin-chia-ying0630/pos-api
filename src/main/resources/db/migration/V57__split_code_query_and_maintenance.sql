-- 將保全入口改為「申請」，並把代碼設定拆成只讀查詢與新增／修改兩個畫面。
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
    (UUID(), 'main-screen', 'function_code', 'MPS00001', 'CREATE', '申請保全變更', 'Y', 'S'),
    (UUID(), 'main-screen', 'function_code', 'MCM00001', 'CODE_TABLE', '查詢代碼對照', 'Y', 'S'),
    (UUID(), 'main-screen', 'function_code', 'MCM00002', 'CODE_MAINTENANCE', '異動代碼對照', 'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'route.change-create', 'change-create', '申請保全變更', 'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'route.code-query', 'code-query', '查詢代碼對照', 'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'route.code-maintenance', 'code-maintenance', '異動代碼對照', 'Y', 'S')
AS incoming
ON DUPLICATE KEY UPDATE
    code_after = incoming.code_after,
    code_description = incoming.code_description,
    active_flag = incoming.active_flag,
    review_status = incoming.review_status;

-- 原本可維護代碼的經辦或管理者延續取得異動畫面；查詢畫面維持原授權。
INSERT IGNORE INTO user_screen_authorization (
    user_screen_authorization_id,
    user_id,
    function_code,
    created_by,
    updated_by
)
SELECT UUID(), screen.user_id, 'MCM00002', 'system', 'system'
FROM user_screen_authorization screen
WHERE screen.function_code = 'MCM00001'
  AND EXISTS (
      SELECT 1
      FROM user_role_assignment authority
      WHERE authority.user_id = screen.user_id
        AND authority.role_code IN ('ROLE_MAKER', 'ROLE_ADMIN')
  );
