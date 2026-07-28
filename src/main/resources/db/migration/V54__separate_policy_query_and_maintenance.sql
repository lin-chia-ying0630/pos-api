-- 查詢頁保持唯讀；所有保單新增、修改與刪除集中到獨立的異動保單服務。
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
    (UUID(), 'main-screen', 'function_code', 'MPM00004', 'POLICY_MAINTENANCE', '異動保單服務', 'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'route.policy-maintenance', 'policy-maintenance', '異動保單服務', 'Y', 'S')
AS incoming
ON DUPLICATE KEY UPDATE
    code_after = incoming.code_after,
    code_description = incoming.code_description,
    active_flag = incoming.active_flag,
    review_status = incoming.review_status;

-- 經辦與管理員預設取得新維護頁；之後仍可由使用者授權頁依 userId 個別調整。
INSERT IGNORE INTO user_screen_authorization (
    user_screen_authorization_id,
    user_id,
    function_code,
    created_by,
    updated_by
)
SELECT UUID(), assignment.user_id, 'MPM00004', 'system', 'system'
FROM user_role_assignment assignment
WHERE assignment.role_code IN ('ROLE_MAKER', 'ROLE_ADMIN');
