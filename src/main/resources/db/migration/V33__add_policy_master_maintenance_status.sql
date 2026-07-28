ALTER TABLE main_policy_master
    ADD COLUMN active_flag VARCHAR(1) NOT NULL DEFAULT 'Y',
    ADD COLUMN review_status VARCHAR(1) NOT NULL DEFAULT 'S';

ALTER TABLE main_policy_master
    ADD CONSTRAINT chk_main_policy_master_active_flag CHECK (active_flag IN ('Y', 'N')),
    ADD CONSTRAINT chk_main_policy_master_review_status CHECK (review_status IN ('P', 'S', 'C'));
