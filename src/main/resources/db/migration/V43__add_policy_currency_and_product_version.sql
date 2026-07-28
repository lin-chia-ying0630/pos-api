ALTER TABLE policy_contract
    ADD COLUMN currency_code CHAR(3) NOT NULL DEFAULT 'TWD' AFTER premium_amount;

ALTER TABLE policy_coverage
    MODIFY COLUMN coverage_item_type VARCHAR(16) NOT NULL,
    ADD COLUMN product_version VARCHAR(32) NOT NULL DEFAULT '1' AFTER product_code,
    ADD COLUMN currency_code CHAR(3) NOT NULL DEFAULT 'TWD' AFTER premium_amount;

UPDATE policy_coverage SET coverage_item_type = CASE
    WHEN coverage_item_type = '1' THEN 'BASE'
    WHEN coverage_item_type IN ('2', '3') THEN 'RIDER'
    ELSE UPPER(coverage_item_type)
END;

ALTER TABLE policy_contract ADD CONSTRAINT chk_policy_contract_currency
    CHECK (currency_code REGEXP '^[A-Z]{3}$');
ALTER TABLE policy_coverage ADD CONSTRAINT chk_policy_coverage_currency
    CHECK (currency_code REGEXP '^[A-Z]{3}$');
