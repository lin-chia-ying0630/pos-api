ALTER TABLE change_review
    ADD COLUMN review_remark VARCHAR(1000) NULL AFTER content_after;
