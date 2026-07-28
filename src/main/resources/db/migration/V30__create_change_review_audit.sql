CREATE TABLE IF NOT EXISTS change_review_audit (
    audit_id BIGINT NOT NULL AUTO_INCREMENT,
    event_id CHAR(36) NOT NULL,
    review_id BIGINT NOT NULL,
    review_key VARCHAR(512) NOT NULL,
    function_code VARCHAR(16) NOT NULL,
    action VARCHAR(32) NOT NULL,
    status_before VARCHAR(1) NULL,
    status_after VARCHAR(1) NOT NULL,
    operator_id VARCHAR(128) NOT NULL,
    review_remark VARCHAR(1000) NULL,
    content_before LONGTEXT NULL,
    content_after LONGTEXT NULL,
    request_id VARCHAR(128) NULL,
    trace_id VARCHAR(128) NULL,
    occurred_at DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (audit_id),
    UNIQUE KEY uk_change_review_audit_event (event_id),
    KEY idx_change_review_audit_review (review_id, occurred_at),
    KEY idx_change_review_audit_key (review_key, occurred_at),
    KEY idx_change_review_audit_operator (operator_id, occurred_at),
    CONSTRAINT fk_change_review_audit_review
        FOREIGN KEY (review_id) REFERENCES change_review(id),
    CONSTRAINT chk_change_review_audit_action
        CHECK (action IN ('SUBMIT', 'APPROVE', 'REJECT', 'RESUBMIT', 'WITHDRAW')),
    CONSTRAINT chk_change_review_audit_status_before
        CHECK (status_before IS NULL OR status_before IN ('P', 'S', 'C')),
    CONSTRAINT chk_change_review_audit_status_after
        CHECK (status_after IN ('P', 'S', 'C'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='覆核機制的不可覆寫稽核歷程，每次狀態事件只新增一筆';
