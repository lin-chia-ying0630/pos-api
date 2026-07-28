ALTER TABLE code_description
    ADD COLUMN review_status VARCHAR(1) NOT NULL DEFAULT 'P',
    ADD COLUMN reviewed_by VARCHAR(64),
    ADD COLUMN reviewed_at DATETIME;
