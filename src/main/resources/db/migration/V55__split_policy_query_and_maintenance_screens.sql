-- 保單服務拆成三個查詢畫面與三個異動畫面，每個畫面獨立授權。
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
    (UUID(), 'main-screen', 'function_code', 'MPM00004', 'MAINTAIN_MASTER', '異動保單主檔', 'Y', 'S'),
    (UUID(), 'main-screen', 'function_code', 'MPM00005', 'MAINTAIN_ADDRESS', '異動保單地址', 'Y', 'S'),
    (UUID(), 'main-screen', 'function_code', 'MPM00006', 'MAINTAIN_RIDE', '異動保單主附約', 'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'route.policy-query', 'policy-query', '查詢保單主檔', 'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'route.policy-address-query', 'policy-address-query', '查詢保單地址', 'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'route.policy-ride-query', 'policy-ride-query', '查詢保單主附約', 'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'route.policy-master-maintenance', 'policy-master-maintenance', '異動保單主檔', 'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'route.policy-address-maintenance', 'policy-address-maintenance', '異動保單地址', 'Y', 'S'),
    (UUID(), 'main-navigation', 'navigation_label', 'route.policy-ride-maintenance', 'policy-ride-maintenance', '異動保單主附約', 'Y', 'S')
AS incoming
ON DUPLICATE KEY UPDATE
    code_after = incoming.code_after,
    code_description = incoming.code_description,
    active_flag = incoming.active_flag,
    review_status = incoming.review_status;

-- 移除已被三個獨立路由取代的舊導覽名稱，避免選單重複。
DELETE FROM code_definition
WHERE code_group = 'main-navigation'
  AND code_field = 'navigation_label'
  AND code_before = 'route.policy-maintenance';

-- 原本持有異動保單服務的人員，延續取得三個獨立異動畫面。
INSERT IGNORE INTO user_screen_authorization (
    user_screen_authorization_id,
    user_id,
    function_code,
    created_by,
    updated_by
)
SELECT UUID(), authorization.user_id, function_code.code, 'system', 'system'
FROM user_screen_authorization authorization
CROSS JOIN (
    SELECT 'MPM00005' AS code
    UNION ALL
    SELECT 'MPM00006'
) function_code
WHERE authorization.function_code = 'MPM00004';
