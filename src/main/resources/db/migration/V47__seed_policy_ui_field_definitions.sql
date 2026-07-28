-- 前端保單維護表單的資料規格來源。
-- code_after 格式：文字為 maxLength；數字為 precision,scale；非長度型別留 NULL。
-- code_description 顯示給維護人員閱讀，不保存任何 px 欄寬或程式旗標。
INSERT INTO code_definition
    (code_group, code_field, code_before, code_after, code_description, active_flag, review_status)
VALUES
    ('UI-field-master', 'policyNo',       'text',     '10',   'required=Y|identity=Y|editable=Y', 'Y', 'S'),
    ('UI-field-master', 'policySeq',      'number',   '10,0', 'required=Y|identity=Y|editable=Y|min=1|step=1', 'Y', 'S'),
    ('UI-field-master', 'premiumAmount',  'number',   '17,4', 'required=Y|identity=N|editable=Y|min=0|step=0.0001', 'Y', 'S'),
    ('UI-field-master', 'currencyCode',   'text',     '3',    'required=Y|identity=N|editable=Y', 'Y', 'S'),
    ('UI-field-master', 'activeFlag',     'select',   '1',    'required=N|identity=N|editable=N|options=Y,N', 'Y', 'S'),
    ('UI-field-master', 'reviewStatus',   'select',   '1',    'required=N|identity=N|editable=N|options=P,S,C', 'Y', 'S'),
    ('UI-field-master', 'recordVersion',  'number',   '19,0', 'required=N|identity=N|editable=N', 'Y', 'S'),
    ('UI-field-master', 'createdBy',      'text',     '128',  'required=N|identity=N|editable=N', 'Y', 'S'),
    ('UI-field-master', 'createdAt',      'datetime', NULL,   'required=N|identity=N|editable=N', 'Y', 'S'),
    ('UI-field-master', 'updatedBy',      'text',     '128',  'required=N|identity=N|editable=N', 'Y', 'S'),
    ('UI-field-master', 'updatedAt',      'datetime', NULL,   'required=N|identity=N|editable=N', 'Y', 'S'),
    ('UI-field-master', 'reviewedBy',     'text',     '128',  'required=N|identity=N|editable=N', 'Y', 'S'),
    ('UI-field-master', 'reviewedAt',     'datetime', NULL,   'required=N|identity=N|editable=N', 'Y', 'S'),

    ('UI-field-address', 'policyNo',        'text',     '10',   'required=Y|identity=Y|editable=Y', 'Y', 'S'),
    ('UI-field-address', 'policySeq',       'number',   '10,0', 'required=Y|identity=Y|editable=Y|min=1|step=1', 'Y', 'S'),
    ('UI-field-address', 'addressTypeCode', 'text',     '2',    'required=Y|identity=Y|editable=Y', 'Y', 'S'),
    ('UI-field-address', 'zipCode3',        'text',     '3',    'required=N|identity=N|editable=Y', 'Y', 'S'),
    ('UI-field-address', 'zipCode2',        'text',     '3',    'required=N|identity=N|editable=Y', 'Y', 'S'),
    ('UI-field-address', 'fullWidthAddress','text',     '255',  'required=N|identity=N|editable=Y|wide=Y', 'Y', 'S'),
    ('UI-field-address', 'halfWidthAddress','text',     '255',  'required=N|identity=N|editable=Y|wide=Y', 'Y', 'S'),
    ('UI-field-address', 'activeFlag',      'select',   '1',    'required=N|identity=N|editable=N|options=Y,N', 'Y', 'S'),
    ('UI-field-address', 'reviewStatus',    'select',   '1',    'required=N|identity=N|editable=N|options=P,S,C', 'Y', 'S'),
    ('UI-field-address', 'recordVersion',   'number',   '19,0', 'required=N|identity=N|editable=N', 'Y', 'S'),
    ('UI-field-address', 'createdBy',       'text',     '128',  'required=N|identity=N|editable=N', 'Y', 'S'),
    ('UI-field-address', 'createdAt',       'datetime', NULL,   'required=N|identity=N|editable=N', 'Y', 'S'),
    ('UI-field-address', 'updatedBy',       'text',     '128',  'required=N|identity=N|editable=N', 'Y', 'S'),
    ('UI-field-address', 'updatedAt',       'datetime', NULL,   'required=N|identity=N|editable=N', 'Y', 'S'),
    ('UI-field-address', 'reviewedBy',      'text',     '128',  'required=N|identity=N|editable=N', 'Y', 'S'),
    ('UI-field-address', 'reviewedAt',      'datetime', NULL,   'required=N|identity=N|editable=N', 'Y', 'S'),

    ('UI-field-ride', 'policyNo',          'text',     '10',   'required=Y|identity=Y|editable=Y', 'Y', 'S'),
    ('UI-field-ride', 'policySeq',         'number',   '10,0', 'required=Y|identity=Y|editable=Y|min=1|step=1', 'Y', 'S'),
    ('UI-field-ride', 'coverageItemSeq',   'text',     '3',    'required=Y|identity=Y|editable=Y', 'Y', 'S'),
    ('UI-field-ride', 'coverageItemType',  'select',   '16',   'required=Y|identity=N|editable=Y|options=BASE,RIDER', 'Y', 'S'),
    ('UI-field-ride', 'productCode',       'text',     '4',    'required=Y|identity=N|editable=Y', 'Y', 'S'),
    ('UI-field-ride', 'productVersion',    'text',     '32',   'required=Y|identity=N|editable=Y', 'Y', 'S'),
    ('UI-field-ride', 'coverageTermYears', 'number',   '10,0', 'required=Y|identity=N|editable=Y|min=1|step=1', 'Y', 'S'),
    ('UI-field-ride', 'insuredAmount',     'number',   '10,2', 'required=Y|identity=N|editable=Y|min=0|step=0.01', 'Y', 'S'),
    ('UI-field-ride', 'premiumAmount',     'number',   '17,4', 'required=Y|identity=N|editable=Y|min=0|step=0.0001', 'Y', 'S'),
    ('UI-field-ride', 'currencyCode',      'text',     '3',    'required=Y|identity=N|editable=Y', 'Y', 'S'),
    ('UI-field-ride', 'activeFlag',        'select',   '1',    'required=N|identity=N|editable=N|options=Y,N', 'Y', 'S'),
    ('UI-field-ride', 'reviewStatus',      'select',   '1',    'required=N|identity=N|editable=N|options=P,S,C', 'Y', 'S'),
    ('UI-field-ride', 'recordVersion',     'number',   '19,0', 'required=N|identity=N|editable=N', 'Y', 'S'),
    ('UI-field-ride', 'createdBy',         'text',     '128',  'required=N|identity=N|editable=N', 'Y', 'S'),
    ('UI-field-ride', 'createdAt',         'datetime', NULL,   'required=N|identity=N|editable=N', 'Y', 'S'),
    ('UI-field-ride', 'updatedBy',         'text',     '128',  'required=N|identity=N|editable=N', 'Y', 'S'),
    ('UI-field-ride', 'updatedAt',         'datetime', NULL,   'required=N|identity=N|editable=N', 'Y', 'S'),
    ('UI-field-ride', 'reviewedBy',        'text',     '128',  'required=N|identity=N|editable=N', 'Y', 'S'),
    ('UI-field-ride', 'reviewedAt',        'datetime', NULL,   'required=N|identity=N|editable=N', 'Y', 'S')
AS incoming
ON DUPLICATE KEY UPDATE
    code_after = incoming.code_after,
    code_description = incoming.code_description,
    active_flag = incoming.active_flag,
    review_status = incoming.review_status;

UPDATE code_definition
SET code_description = CASE
    WHEN code_before = 'number' AND code_after IS NOT NULL THEN CONCAT(
        '前端欄位規格：數字，總位數 ', SUBSTRING_INDEX(code_after, ',', 1),
        '，小數 ', SUBSTRING_INDEX(code_after, ',', -1), ' 位'
    )
    WHEN code_before IN ('text', 'select') AND code_after IS NOT NULL THEN CONCAT(
        '前端欄位規格：', IF(code_before = 'select', '選項', '文字'),
        '，最大 ', code_after, ' 字'
    )
    WHEN code_before = 'datetime' THEN '前端欄位規格：日期時間'
    ELSE '前端欄位規格'
END
WHERE code_group IN ('UI-field-master', 'UI-field-address', 'UI-field-ride');
