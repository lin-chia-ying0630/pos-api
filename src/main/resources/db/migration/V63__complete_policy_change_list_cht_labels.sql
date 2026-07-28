-- 保全受理清單完全由 API 欄位動態展開；新增 API 欄位時，中文名稱必須集中維護於 CHT-code。
-- 前端若找不到中文才會顯示英文 key，因此在資料庫版本中補齊，不於 Vue 畫面寫死。
INSERT INTO code_definition (
    code_definition_id,
    code_group,
    code_field,
    code_before,
    code_after,
    code_description,
    active_flag,
    review_status,
    created_by,
    created_at,
    reviewed_by,
    reviewed_at
)
VALUES
    (UUID(), 'CHT-code', 'acceptanceStatusDescription', '保全受理狀態說明',
     NULL, '保全受理狀態中文說明', 'Y', 'S', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP),
    (UUID(), 'CHT-code', 'changeItemCodeCodes', '變更項目代碼',
     NULL, '保全變更項目代碼集合', 'Y', 'S', 'system', CURRENT_TIMESTAMP, 'system', CURRENT_TIMESTAMP)
AS incoming
ON DUPLICATE KEY UPDATE
    code_before = incoming.code_before,
    code_after = incoming.code_after,
    code_description = incoming.code_description,
    active_flag = incoming.active_flag,
    review_status = incoming.review_status,
    reviewed_by = COALESCE(code_definition.reviewed_by, incoming.reviewed_by),
    reviewed_at = COALESCE(code_definition.reviewed_at, incoming.reviewed_at);
