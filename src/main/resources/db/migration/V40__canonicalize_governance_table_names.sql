-- 將 Spring Security 框架預設表名改為企業 DD 業務名稱，授權一律掛 user_id。
RENAME TABLE users TO user_account;
RENAME TABLE authorities TO user_role_assignment;
RENAME TABLE code_description TO code_definition;

ALTER TABLE user_account
    RENAME COLUMN username TO user_id;

ALTER TABLE user_role_assignment
    RENAME COLUMN username TO user_id,
    RENAME COLUMN authority TO role_code;

ALTER TABLE user_screen_authorization
    RENAME COLUMN username TO user_id;

UPDATE code_definition
SET code_group = 'user-authorization', code_field = 'role_code'
WHERE code_group = 'main-user' AND code_field = 'authorities';
