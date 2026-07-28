DELETE FROM code_description
WHERE code_group IN ('role', 'user-authorization');

DELETE FROM code_description
WHERE code_group = 'main-code'
  AND code_field IN ('role', 'user-authorization');
