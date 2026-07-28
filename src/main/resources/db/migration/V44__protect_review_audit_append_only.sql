CREATE TRIGGER trg_change_review_audit_no_update
BEFORE UPDATE ON change_review_audit
FOR EACH ROW
SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'change_review_audit is append-only';

CREATE TRIGGER trg_change_review_audit_no_delete
BEFORE DELETE ON change_review_audit
FOR EACH ROW
SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'change_review_audit is append-only';
