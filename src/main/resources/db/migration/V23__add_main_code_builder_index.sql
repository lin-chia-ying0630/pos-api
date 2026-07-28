INSERT INTO code_description
(
    code_group,
    code_field,
    code_before,
    code_after,
    code_description
)
VALUES ('main-code', 'main-code', '001', '', '代碼建置')
ON DUPLICATE KEY UPDATE
    code_after = VALUES(code_after),
    code_description = VALUES(code_description);
