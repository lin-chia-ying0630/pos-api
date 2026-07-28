INSERT INTO code_definition
    (code_group, code_field, code_before, code_after, code_description, active_flag, review_status)
VALUES
    ('CHT-code', 'userId', '使用者 ID', NULL, '使用者 ID', 'Y', 'S'),
    ('CHT-code', 'functionCode', '功能代碼', NULL, '功能代碼', 'Y', 'S')
AS incoming
ON DUPLICATE KEY UPDATE
    code_after = incoming.code_after,
    code_description = incoming.code_description,
    active_flag = incoming.active_flag,
    review_status = incoming.review_status;
