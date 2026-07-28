ALTER TABLE code_description
    ADD COLUMN active_flag CHAR(1) NOT NULL DEFAULT 'Y' AFTER code_description;
