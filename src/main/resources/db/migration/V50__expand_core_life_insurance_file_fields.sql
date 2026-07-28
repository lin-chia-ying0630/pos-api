-- 補齊目前三個核心壽險資料檔的契約、聯絡及保障項目欄位。
-- 新欄位先允許 NULL，避免對既有保單臆測日期、狀態、商品或聯絡資料。
ALTER TABLE policy_contract
    ADD COLUMN policy_status VARCHAR(2) NULL AFTER currency_code,
    ADD COLUMN contract_date DATE NULL AFTER policy_status,
    ADD COLUMN effective_date DATE NULL AFTER contract_date,
    ADD COLUMN maturity_date DATE NULL AFTER effective_date,
    ADD COLUMN premium_payment_term_years INT NULL AFTER maturity_date,
    ADD COLUMN coverage_term_years INT NULL AFTER premium_payment_term_years,
    ADD COLUMN coverage_term_type VARCHAR(8) NULL AFTER coverage_term_years,
    ADD COLUMN payment_frequency_code VARCHAR(4) NULL AFTER coverage_term_type,
    ADD COLUMN product_code VARCHAR(32) NULL AFTER payment_frequency_code,
    ADD COLUMN product_version VARCHAR(32) NULL AFTER product_code,
    ADD COLUMN product_name VARCHAR(200) NULL AFTER product_version,
    ADD COLUMN base_plan_product_code VARCHAR(32) NULL AFTER product_name,
    ADD COLUMN application_no VARCHAR(32) NULL AFTER base_plan_product_code,
    ADD COLUMN customer_code VARCHAR(32) NULL AFTER application_no,
    ADD COLUMN insurance_agent_code VARCHAR(32) NULL AFTER customer_code;

ALTER TABLE policy_contact
    ADD COLUMN postal_code VARCHAR(6) NULL AFTER half_width_address,
    ADD COLUMN address_text VARCHAR(300) NULL AFTER postal_code,
    ADD COLUMN email_address VARCHAR(254) NULL AFTER address_text,
    ADD COLUMN telephone_no VARCHAR(30) NULL AFTER email_address,
    ADD COLUMN mobile_no VARCHAR(30) NULL AFTER telephone_no;

-- 只回填可由舊欄位明確推導的資料；不依地址類型猜測 email、電話或手機。
UPDATE policy_contact
SET postal_code = NULLIF(CONCAT(COALESCE(zip_code3, ''), COALESCE(zip_code2, '')), ''),
    address_text = full_width_address
WHERE postal_code IS NULL AND address_text IS NULL;

ALTER TABLE policy_coverage
    ADD COLUMN product_name VARCHAR(200) NULL AFTER product_version,
    ADD COLUMN base_plan_product_code VARCHAR(32) NULL AFTER product_name,
    ADD COLUMN payment_frequency_code VARCHAR(4) NULL AFTER currency_code,
    ADD COLUMN premium_payment_term_years INT NULL AFTER payment_frequency_code,
    ADD COLUMN coverage_term_type VARCHAR(8) NULL AFTER premium_payment_term_years,
    ADD COLUMN effective_date DATE NULL AFTER coverage_term_type,
    ADD COLUMN expiry_date DATE NULL AFTER effective_date;

-- API 表單 metadata：英文 key、型態、容量及繁體中文說明均由資料庫提供。
INSERT INTO code_definition
    (code_group, code_field, code_before, code_after, code_description, active_flag, review_status)
VALUES
    ('UI-field-master', 'policyStatus', 'text', '2', '保單狀態代碼，最多 2 碼', 'Y', 'S'),
    ('UI-field-master', 'contractDate', 'date', NULL, '保險契約成立日期', 'Y', 'S'),
    ('UI-field-master', 'effectiveDate', 'date', NULL, '保單責任生效日期', 'Y', 'S'),
    ('UI-field-master', 'maturityDate', 'date', NULL, '保單契約滿期日期', 'Y', 'S'),
    ('UI-field-master', 'premiumPaymentTermYears', 'number', '10,0', '繳費年期', 'Y', 'S'),
    ('UI-field-master', 'coverageTermYears', 'number', '10,0', '保險期間年期', 'Y', 'S'),
    ('UI-field-master', 'coverageTermType', 'text', '8', '保險期間類型代碼', 'Y', 'S'),
    ('UI-field-master', 'paymentFrequencyCode', 'text', '4', '保費繳別代碼', 'Y', 'S'),
    ('UI-field-master', 'productCode', 'text', '32', '保險商品代碼', 'Y', 'S'),
    ('UI-field-master', 'productVersion', 'text', '32', '保險商品版本', 'Y', 'S'),
    ('UI-field-master', 'productName', 'text', '200', '核准商品名稱', 'Y', 'S'),
    ('UI-field-master', 'basePlanProductCode', 'text', '32', '主約商品代碼', 'Y', 'S'),
    ('UI-field-master', 'applicationNo', 'text', '32', '要保書號碼', 'Y', 'S'),
    ('UI-field-master', 'customerCode', 'text', '32', '客戶代碼', 'Y', 'S'),
    ('UI-field-master', 'insuranceAgentCode', 'text', '32', '保險業務員代碼', 'Y', 'S'),
    ('UI-field-address', 'postalCode', 'text', '6', '郵遞區號，最多 6 碼', 'Y', 'S'),
    ('UI-field-address', 'addressText', 'text', '300', '完整地址文字', 'Y', 'S'),
    ('UI-field-address', 'emailAddress', 'email', '254', '電子郵件地址', 'Y', 'S'),
    ('UI-field-address', 'telephoneNo', 'text', '30', '市內電話號碼', 'Y', 'S'),
    ('UI-field-address', 'mobileNo', 'text', '30', '行動電話號碼', 'Y', 'S'),
    ('UI-field-ride', 'productName', 'text', '200', '保障項目商品名稱', 'Y', 'S'),
    ('UI-field-ride', 'basePlanProductCode', 'text', '32', '所依附主約商品代碼', 'Y', 'S'),
    ('UI-field-ride', 'paymentFrequencyCode', 'text', '4', '保障項目繳別代碼', 'Y', 'S'),
    ('UI-field-ride', 'premiumPaymentTermYears', 'number', '10,0', '保障項目繳費年期', 'Y', 'S'),
    ('UI-field-ride', 'coverageTermType', 'text', '8', '保障期間類型代碼', 'Y', 'S'),
    ('UI-field-ride', 'effectiveDate', 'date', NULL, '保障項目生效日期', 'Y', 'S'),
    ('UI-field-ride', 'expiryDate', 'date', NULL, '保障項目終止日期', 'Y', 'S')
AS incoming
ON DUPLICATE KEY UPDATE
    code_before = incoming.code_before,
    code_after = incoming.code_after,
    code_description = incoming.code_description;

INSERT INTO code_definition
    (code_group, code_field, code_before, code_after, code_description, active_flag, review_status)
VALUES
    ('CHT-code', 'policyStatus', '保單狀態', NULL, '保單狀態', 'Y', 'S'),
    ('CHT-code', 'contractDate', '契約成立日', NULL, '契約成立日', 'Y', 'S'),
    ('CHT-code', 'effectiveDate', '生效日', NULL, '生效日', 'Y', 'S'),
    ('CHT-code', 'maturityDate', '滿期日', NULL, '滿期日', 'Y', 'S'),
    ('CHT-code', 'premiumPaymentTermYears', '繳費年期', NULL, '繳費年期', 'Y', 'S'),
    ('CHT-code', 'coverageTermYears', '保險期間年期', NULL, '保險期間年期', 'Y', 'S'),
    ('CHT-code', 'coverageTermType', '保險期間類型', NULL, '保險期間類型', 'Y', 'S'),
    ('CHT-code', 'paymentFrequencyCode', '繳別代碼', NULL, '繳別代碼', 'Y', 'S'),
    ('CHT-code', 'productCode', '商品代碼', NULL, '商品代碼', 'Y', 'S'),
    ('CHT-code', 'productVersion', '商品版本', NULL, '商品版本', 'Y', 'S'),
    ('CHT-code', 'productName', '商品名稱', NULL, '商品名稱', 'Y', 'S'),
    ('CHT-code', 'basePlanProductCode', '主約商品代碼', NULL, '主約商品代碼', 'Y', 'S'),
    ('CHT-code', 'applicationNo', '要保書號碼', NULL, '要保書號碼', 'Y', 'S'),
    ('CHT-code', 'customerCode', '客戶代碼', NULL, '客戶代碼', 'Y', 'S'),
    ('CHT-code', 'insuranceAgentCode', '業務員代碼', NULL, '業務員代碼', 'Y', 'S'),
    ('CHT-code', 'postalCode', '郵遞區號', NULL, '郵遞區號', 'Y', 'S'),
    ('CHT-code', 'addressText', '地址', NULL, '地址', 'Y', 'S'),
    ('CHT-code', 'emailAddress', '電子郵件', NULL, '電子郵件', 'Y', 'S'),
    ('CHT-code', 'telephoneNo', '市內電話', NULL, '市內電話', 'Y', 'S'),
    ('CHT-code', 'mobileNo', '行動電話', NULL, '行動電話', 'Y', 'S'),
    ('CHT-code', 'expiryDate', '終止日', NULL, '終止日', 'Y', 'S')
AS incoming
ON DUPLICATE KEY UPDATE
    code_before = incoming.code_before,
    code_description = incoming.code_description;
