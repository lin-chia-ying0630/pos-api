-- Local demo 資料只負責補齊不存在的種子，不得在每次啟動時覆寫已套用的保全結果。
INSERT IGNORE INTO policy_contract
    (policy_contract_id, policy_no, policy_seq, premium_amount, currency_code, active_flag, review_status)
VALUES (UUID(), 'P000000001', 1, 15925.9089, 'TWD', 'Y', 'S');

INSERT IGNORE INTO policy_contact_address
    (address_id, policy_no, policy_seq, address_type_code, postal_code, address_text, country_code,
     primary_flag, active_flag, review_status)
VALUES
    (UUID(), 'P000000001', 1, '01', '100001', '臺北市中正區重慶南路一段１號', 'TW', 'Y', 'Y', 'S'),
    (UUID(), 'P000000001', 1, '02', '104001', '臺北市中山區南京東路二段１００號', 'TW', 'N', 'Y', 'S');

INSERT IGNORE INTO policy_contact_email
    (email_id, policy_no, policy_seq, email_type_code, email_address, primary_flag, active_flag, review_status)
VALUES (UUID(), 'P000000001', 1, '01', 'policyholder@example.com', 'Y', 'Y', 'S');

INSERT IGNORE INTO policy_coverage
    (coverage_id, policy_no, policy_seq, coverage_item_type, coverage_item_seq, product_code,
     product_version, coverage_term_years, insured_amount, premium_amount, currency_code,
     active_flag, review_status)
VALUES
    (UUID(), 'P000000001', 1, 'BASE', '000', 'LIFE', '1', 20, 1000000.00, 12345.6789, 'TWD', 'Y', 'S'),
    (UUID(), 'P000000001', 1, 'RIDER', '001', 'ADDR', '1', 20, 500000.00, 2345.6700, 'TWD', 'Y', 'S'),
    (UUID(), 'P000000001', 1, 'RIDER', '002', 'FAMI', '1', 20, 300000.00, 1234.5600, 'TWD', 'Y', 'S');
