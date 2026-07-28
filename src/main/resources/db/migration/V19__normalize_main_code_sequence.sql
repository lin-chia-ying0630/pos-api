DELETE FROM code_description
WHERE code_group = 'main-code'
  AND code_before = '1';

DELETE FROM code_description
WHERE code_group = 'main-code'
  AND code_field IN ('main-user', 'main-screen');
