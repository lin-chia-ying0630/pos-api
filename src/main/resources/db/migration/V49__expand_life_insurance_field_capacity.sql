-- 依專案壽險 DD 擴充正式資料容量。
-- 只新增遷移，不修改已執行版本；FK 需先移除，所有關聯欄位同步擴充後再重建。
ALTER TABLE policy_change_record_snapshot DROP FOREIGN KEY fk_change_file_item;
ALTER TABLE policy_change_field DROP FOREIGN KEY fk_change_field_item;
ALTER TABLE policy_change_item DROP FOREIGN KEY fk_change_item_acceptance;
ALTER TABLE policy_change_acceptance DROP FOREIGN KEY fk_change_acceptance_master;
ALTER TABLE policy_contact DROP FOREIGN KEY fk_policy_address_master;
ALTER TABLE policy_coverage DROP FOREIGN KEY fk_policy_ride_master;
ALTER TABLE policy_change_case_reservation DROP FOREIGN KEY fk_change_case_reservation_master;

ALTER TABLE policy_contract
    MODIFY COLUMN policy_no VARCHAR(20) NOT NULL,
    MODIFY COLUMN premium_amount DECIMAL(18, 4) NOT NULL;

ALTER TABLE policy_contact
    MODIFY COLUMN policy_no VARCHAR(20) NOT NULL,
    MODIFY COLUMN address_type_code VARCHAR(8) NOT NULL,
    MODIFY COLUMN full_width_address VARCHAR(300) NULL,
    MODIFY COLUMN half_width_address VARCHAR(300) NULL;

ALTER TABLE policy_coverage
    MODIFY COLUMN policy_no VARCHAR(20) NOT NULL,
    MODIFY COLUMN coverage_item_seq VARCHAR(10) NOT NULL,
    MODIFY COLUMN product_code VARCHAR(32) NOT NULL,
    MODIFY COLUMN insured_amount DECIMAL(18, 2) NOT NULL,
    MODIFY COLUMN premium_amount DECIMAL(18, 4) NOT NULL;

ALTER TABLE policy_change_acceptance MODIFY COLUMN policy_no VARCHAR(20) NOT NULL;
ALTER TABLE policy_change_item MODIFY COLUMN policy_no VARCHAR(20) NOT NULL;
ALTER TABLE policy_change_field MODIFY COLUMN policy_no VARCHAR(20) NOT NULL;
ALTER TABLE policy_change_record_snapshot MODIFY COLUMN policy_no VARCHAR(20) NOT NULL;
ALTER TABLE policy_change_case_reservation MODIFY COLUMN policy_no VARCHAR(20) NOT NULL;
ALTER TABLE change_review MODIFY COLUMN policy_no VARCHAR(20) NULL;

ALTER TABLE policy_contact
    ADD CONSTRAINT fk_policy_address_master
        FOREIGN KEY (policy_no, policy_seq) REFERENCES policy_contract (policy_no, policy_seq);
ALTER TABLE policy_coverage
    ADD CONSTRAINT fk_policy_ride_master
        FOREIGN KEY (policy_no, policy_seq) REFERENCES policy_contract (policy_no, policy_seq);
ALTER TABLE policy_change_acceptance
    ADD CONSTRAINT fk_change_acceptance_master
        FOREIGN KEY (policy_no, policy_seq) REFERENCES policy_contract (policy_no, policy_seq);
ALTER TABLE policy_change_item
    ADD CONSTRAINT fk_change_item_acceptance
        FOREIGN KEY (policy_no, policy_seq, change_case_no)
        REFERENCES policy_change_acceptance (policy_no, policy_seq, change_case_no);
ALTER TABLE policy_change_field
    ADD CONSTRAINT fk_change_field_item
        FOREIGN KEY (policy_no, policy_seq, change_case_no, change_item_code)
        REFERENCES policy_change_item (policy_no, policy_seq, change_case_no, change_item_code);
ALTER TABLE policy_change_record_snapshot
    ADD CONSTRAINT fk_change_file_item
        FOREIGN KEY (policy_no, policy_seq, change_case_no, change_item_code)
        REFERENCES policy_change_item (policy_no, policy_seq, change_case_no, change_item_code);
ALTER TABLE policy_change_case_reservation
    ADD CONSTRAINT fk_change_case_reservation_master
        FOREIGN KEY (policy_no, policy_seq) REFERENCES policy_contract (policy_no, policy_seq);

-- 表單 metadata 與資料庫容量同步；前端 maxlength/precision 及後端動態驗證都由此取得。
UPDATE code_definition
SET code_after = '20', code_description = '保單號碼最多 20 碼英數字'
WHERE code_group IN ('UI-field-master', 'UI-field-address', 'UI-field-ride')
  AND code_field = 'policyNo';

UPDATE code_definition
SET code_after = '18,4', code_description = '保費最多 14 位整數及 4 位小數'
WHERE code_group IN ('UI-field-master', 'UI-field-ride')
  AND code_field = 'premiumAmount';

UPDATE code_definition
SET code_after = '8', code_description = '地址類型代碼最多 8 碼'
WHERE code_group = 'UI-field-address' AND code_field = 'addressTypeCode';

UPDATE code_definition
SET code_after = '300', code_description = '聯絡或地址內容最多 300 個字元'
WHERE code_group = 'UI-field-address'
  AND code_field IN ('fullWidthAddress', 'halfWidthAddress');

UPDATE code_definition
SET code_after = '10', code_description = '保障項目序號最多 10 碼'
WHERE code_group = 'UI-field-ride' AND code_field = 'coverageItemSeq';

UPDATE code_definition
SET code_after = '32', code_description = '商品代碼最多 32 碼'
WHERE code_group = 'UI-field-ride' AND code_field = 'productCode';

UPDATE code_definition
SET code_after = '18,2', code_description = '保險金額最多 16 位整數及 2 位小數'
WHERE code_group = 'UI-field-ride' AND code_field = 'insuredAmount';
