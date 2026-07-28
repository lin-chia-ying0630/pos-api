-- 將既有壽險核心欄位改為 DD canonical name。
-- 既有 Flyway 不回寫；由本版本保留資料並原地改名，讓升級庫與空庫走相同路徑。
ALTER TABLE main_policy_master
    RENAME COLUMN premium TO premium_amount;

ALTER TABLE main_policy_address
    RENAME COLUMN address_type TO address_type_code;

ALTER TABLE main_policy_ride
    RENAME COLUMN ride_type TO rider_type,
    RENAME COLUMN ride_order TO rider_seq,
    RENAME COLUMN policy_years TO coverage_term_years,
    RENAME COLUMN premium TO premium_amount;

-- 中文欄位 metadata 同步 canonical API key；舊覆核 JSON 仍由服務相容解析。
UPDATE code_description
SET code_field = 'addressTypeCode'
WHERE code_group = 'CHT-code' AND code_field = 'addressType';

UPDATE code_description
SET code_field = 'riderType'
WHERE code_group = 'CHT-code' AND code_field = 'rideType';

UPDATE code_description
SET code_field = 'riderSeq'
WHERE code_group = 'CHT-code' AND code_field = 'rideOrder';

UPDATE code_description
SET code_field = 'coverageTermYears'
WHERE code_group = 'CHT-code' AND code_field IN ('policyYears', 'mainPolicyYears');

UPDATE code_description
SET code_field = 'premiumAmount'
WHERE code_group = 'CHT-code' AND code_field = 'premium';

INSERT INTO code_description (code_group, code_field, code_before, code_after, code_description)
VALUES
    ('CHT-code', 'addressTypeCode', 'addressTypeCode', NULL, '地址類型代碼'),
    ('CHT-code', 'riderType', 'riderType', NULL, '附約類型'),
    ('CHT-code', 'riderSeq', 'riderSeq', NULL, '附約序號'),
    ('CHT-code', 'coverageTermYears', 'coverageTermYears', NULL, '保險期間年期'),
    ('CHT-code', 'premiumAmount', 'premiumAmount', NULL, '保費金額')
AS incoming
ON DUPLICATE KEY UPDATE code_description = incoming.code_description;
