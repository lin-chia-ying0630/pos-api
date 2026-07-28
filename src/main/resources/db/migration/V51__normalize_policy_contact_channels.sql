-- 聯絡資料依業務種類正規化；不再以 full/half width 欄位混放地址、Email 與電話。
RENAME TABLE policy_contact TO policy_contact_source;

CREATE TABLE policy_contact_address (
    address_id CHAR(36) NOT NULL,
    policy_no VARCHAR(20) NOT NULL,
    policy_seq INT NOT NULL,
    address_type_code VARCHAR(8) NOT NULL,
    postal_code VARCHAR(6) NOT NULL,
    address_text VARCHAR(300) NOT NULL,
    country_code CHAR(2) NOT NULL DEFAULT 'TW',
    primary_flag CHAR(1) NOT NULL DEFAULT 'N',
    active_flag CHAR(1) NOT NULL DEFAULT 'Y',
    review_status CHAR(1) NOT NULL DEFAULT 'S',
    created_by VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by VARCHAR(100),
    reviewed_at DATETIME,
    record_version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (address_id),
    UNIQUE KEY uk_policy_contact_address (policy_no, policy_seq, address_type_code),
    CONSTRAINT fk_policy_contact_address_contract
        FOREIGN KEY (policy_no, policy_seq) REFERENCES policy_contract (policy_no, policy_seq)
);

CREATE TABLE policy_contact_email (
    email_id CHAR(36) NOT NULL,
    policy_no VARCHAR(20) NOT NULL,
    policy_seq INT NOT NULL,
    email_type_code VARCHAR(8) NOT NULL,
    email_address VARCHAR(254) NOT NULL,
    primary_flag CHAR(1) NOT NULL DEFAULT 'N',
    active_flag CHAR(1) NOT NULL DEFAULT 'Y',
    review_status CHAR(1) NOT NULL DEFAULT 'S',
    created_by VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by VARCHAR(100),
    reviewed_at DATETIME,
    record_version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (email_id),
    KEY ix_policy_contact_email (policy_no, policy_seq),
    CONSTRAINT fk_policy_contact_email_contract
        FOREIGN KEY (policy_no, policy_seq) REFERENCES policy_contract (policy_no, policy_seq)
);

CREATE TABLE policy_contact_phone (
    phone_id CHAR(36) NOT NULL,
    policy_no VARCHAR(20) NOT NULL,
    policy_seq INT NOT NULL,
    phone_type_code VARCHAR(8) NOT NULL,
    country_calling_code VARCHAR(4) NOT NULL DEFAULT '886',
    area_code VARCHAR(6),
    phone_number VARCHAR(24) NOT NULL,
    extension_no VARCHAR(10),
    primary_flag CHAR(1) NOT NULL DEFAULT 'N',
    active_flag CHAR(1) NOT NULL DEFAULT 'Y',
    review_status CHAR(1) NOT NULL DEFAULT 'S',
    created_by VARCHAR(100),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_by VARCHAR(100),
    reviewed_at DATETIME,
    record_version BIGINT NOT NULL DEFAULT 0,
    PRIMARY KEY (phone_id),
    KEY ix_policy_contact_phone (policy_no, policy_seq),
    CONSTRAINT fk_policy_contact_phone_contract
        FOREIGN KEY (policy_no, policy_seq) REFERENCES policy_contract (policy_no, policy_seq)
);

-- 既有資料依明確的地址類型拆分；不以內容猜測資料來源。
INSERT INTO policy_contact_address (
    address_id, policy_no, policy_seq, address_type_code, postal_code, address_text,
    primary_flag, active_flag, review_status, created_by, created_at, updated_by,
    updated_at, reviewed_by, reviewed_at, record_version
)
SELECT UUID(), policy_no, policy_seq, address_type_code,
       COALESCE(NULLIF(postal_code, ''), CONCAT(COALESCE(zip_code3, ''), COALESCE(zip_code2, ''))),
       COALESCE(NULLIF(address_text, ''), full_width_address),
       CASE WHEN address_type_code = '01' THEN 'Y' ELSE 'N' END,
       active_flag, review_status, created_by, created_at, updated_by, updated_at,
       reviewed_by, reviewed_at, record_version
FROM policy_contact_source
WHERE address_type_code IN ('01', '02');

INSERT INTO policy_contact_email (
    email_id, policy_no, policy_seq, email_type_code, email_address, primary_flag,
    active_flag, review_status, created_by, created_at, updated_by, updated_at,
    reviewed_by, reviewed_at, record_version
)
SELECT UUID(), policy_no, policy_seq, address_type_code,
       COALESCE(NULLIF(email_address, ''), half_width_address), 'Y',
       active_flag, review_status, created_by, created_at, updated_by, updated_at,
       reviewed_by, reviewed_at, record_version
FROM policy_contact_source
WHERE address_type_code = '31'
  AND COALESCE(NULLIF(email_address, ''), NULLIF(half_width_address, '')) IS NOT NULL;

INSERT INTO policy_contact_phone (
    phone_id, policy_no, policy_seq, phone_type_code, phone_number, primary_flag,
    active_flag, review_status, created_by, created_at, updated_by, updated_at,
    reviewed_by, reviewed_at, record_version
)
SELECT UUID(), policy_no, policy_seq, address_type_code,
       COALESCE(NULLIF(telephone_no, ''), half_width_address),
       CASE WHEN address_type_code = '11' THEN 'Y' ELSE 'N' END,
       active_flag, review_status, created_by, created_at, updated_by, updated_at,
       reviewed_by, reviewed_at, record_version
FROM policy_contact_source
WHERE address_type_code IN ('11', '12')
  AND COALESCE(NULLIF(telephone_no, ''), NULLIF(half_width_address, '')) IS NOT NULL;

DROP TABLE policy_contact_source;

UPDATE code_definition
SET active_flag = 'N'
WHERE code_group IN ('UI-field-address', 'CHT-code')
  AND code_field IN ('zipCode3', 'zipCode2', 'fullWidthAddress', 'halfWidthAddress',
                     'emailAddress', 'telephoneNo', 'mobileNo');

INSERT INTO code_definition
    (code_group, code_field, code_before, code_after, code_description, active_flag, review_status)
VALUES
    ('policy-change-item', 'change_item_code', '004', NULL, '電子郵件變更', 'Y', 'S'),
    ('policy-change-item', 'change_item_code', '005', NULL, '市內電話變更', 'Y', 'S'),
    ('policy-change-item', 'change_item_code', '006', NULL, '行動電話變更', 'Y', 'S'),
    ('CHT-code', 'addressId', '地址識別碼', NULL, '地址識別碼', 'Y', 'S'),
    ('CHT-code', 'emailId', '電子郵件識別碼', NULL, '電子郵件識別碼', 'Y', 'S'),
    ('CHT-code', 'phoneId', '電話識別碼', NULL, '電話識別碼', 'Y', 'S'),
    ('CHT-code', 'primaryFlag', '主要聯絡方式', NULL, '主要聯絡方式', 'Y', 'S')
AS incoming
ON DUPLICATE KEY UPDATE code_description = incoming.code_description;

INSERT INTO code_definition
    (code_group, code_field, code_before, code_after, code_description, active_flag, review_status)
VALUES
    ('UI-field-address', 'addressId', 'text', '36', 'required=N|identity=Y|editable=N', 'Y', 'S'),
    ('UI-field-address', 'addressTypeCode', 'text', '8', 'required=Y|identity=N|editable=Y', 'Y', 'S'),
    ('UI-field-address', 'postalCode', 'text', '6', 'required=Y|identity=N|editable=Y', 'Y', 'S'),
    ('UI-field-address', 'addressText', 'text', '300', 'required=Y|identity=N|editable=Y|wide=Y', 'Y', 'S'),
    ('UI-field-address', 'countryCode', 'text', '2', 'required=Y|identity=N|editable=Y', 'Y', 'S'),
    ('UI-field-address', 'primaryFlag', 'select', '1', 'required=Y|identity=N|editable=Y|options=Y,N', 'Y', 'S')
AS incoming
ON DUPLICATE KEY UPDATE
    code_before = incoming.code_before,
    code_after = incoming.code_after,
    code_description = incoming.code_description,
    active_flag = incoming.active_flag,
    review_status = incoming.review_status;
