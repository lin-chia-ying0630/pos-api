ALTER TABLE code_description
    ADD COLUMN created_by VARCHAR(64) NULL AFTER active_flag,
    ADD COLUMN created_at DATETIME NULL AFTER created_by,
    ADD COLUMN updated_by VARCHAR(64) NULL AFTER created_at,
    ADD COLUMN updated_at DATETIME NULL AFTER updated_by;

UPDATE code_description SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
