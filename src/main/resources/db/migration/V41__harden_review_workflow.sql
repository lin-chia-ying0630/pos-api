-- 正式資料版本供覆核套用時執行樂觀鎖，避免覆蓋送審後的其他異動。
ALTER TABLE policy_contract ADD COLUMN record_version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE policy_contact ADD COLUMN record_version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE policy_coverage ADD COLUMN record_version BIGINT NOT NULL DEFAULT 0;
ALTER TABLE code_definition ADD COLUMN record_version BIGINT NOT NULL DEFAULT 0;

-- 覆核查詢使用結構化 key1，不再掃描含個資的 before/after JSON。
ALTER TABLE change_review ADD COLUMN key1 VARCHAR(128) NULL AFTER function_code;
ALTER TABLE change_review ADD COLUMN workflow_mode VARCHAR(16) NOT NULL DEFAULT 'LEGACY' AFTER operation;
UPDATE change_review
SET key1 = COALESCE(policy_no, change_case_no, SUBSTRING_INDEX(unique_key, '|', 1))
WHERE key1 IS NULL;
CREATE INDEX idx_change_review_key1 ON change_review (function_code, key1, created_at);
ALTER TABLE change_review ADD CONSTRAINT chk_change_review_workflow_mode
    CHECK (workflow_mode IN ('LEGACY', 'STAGED', 'DIRECT'));

-- 相同功能及業務鍵同時間只能有一筆待處理案件。
CREATE TABLE change_review_pending_lock (
    function_code VARCHAR(16) NOT NULL,
    unique_key VARCHAR(255) NOT NULL,
    review_key VARCHAR(512) NOT NULL,
    created_by VARCHAR(128) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (function_code, unique_key),
    KEY idx_change_review_pending_review (review_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='覆核待處理業務鍵唯一鎖，S/C 完成後釋放';

-- 既有 P 案件每個業務鍵只保留最新案件的鎖；歷史重複案仍保留供人工處理。
INSERT IGNORE INTO change_review_pending_lock
    (function_code, unique_key, review_key, created_by, created_at)
SELECT review.function_code, review.unique_key, review.review_key, review.created_by, review.created_at
FROM change_review review
INNER JOIN (
    SELECT function_code, unique_key, MAX(id) latest_id
    FROM change_review
    WHERE review_status = 'P'
    GROUP BY function_code, unique_key
) latest ON latest.latest_id = review.id;

-- 新資料使用 canonical source type；Service 仍保留舊值解析，確保既有歷史可讀。
UPDATE change_review SET source_type = 'POLICY_CONTRACT', source_record_type = 'CONTRACT'
WHERE source_type = 'POLICY_MASTER';
UPDATE change_review SET source_type = 'POLICY_CONTACT', source_record_type = 'CONTACT'
WHERE source_type = 'POLICY_ADDRESS';
UPDATE change_review SET source_type = 'POLICY_COVERAGE', source_record_type = 'COVERAGE'
WHERE source_type = 'POLICY_RIDE';
UPDATE change_review SET source_type = 'CODE_DEFINITION', source_record_type = 'CODE_DEFINITION'
WHERE source_type = 'CODE_TABLE';
