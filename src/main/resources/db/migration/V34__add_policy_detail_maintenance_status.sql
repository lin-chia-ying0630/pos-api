ALTER TABLE main_policy_address
    ADD COLUMN active_flag VARCHAR(1) NOT NULL DEFAULT 'Y',
    ADD COLUMN review_status VARCHAR(1) NOT NULL DEFAULT 'S';

ALTER TABLE main_policy_ride
    ADD COLUMN active_flag VARCHAR(1) NOT NULL DEFAULT 'Y',
    ADD COLUMN review_status VARCHAR(1) NOT NULL DEFAULT 'S';

