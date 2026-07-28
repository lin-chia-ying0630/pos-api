ALTER TABLE change_review_audit DROP CHECK chk_change_review_audit_action;
ALTER TABLE change_review_audit ADD CONSTRAINT chk_change_review_audit_action
    CHECK (action IN ('SUBMIT', 'APPROVE', 'REJECT', 'RESUBMIT', 'WITHDRAW', 'DIRECT_APPLY'));
