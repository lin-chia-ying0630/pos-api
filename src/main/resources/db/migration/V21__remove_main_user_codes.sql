DELETE FROM code_description
WHERE code_group = 'main-user';

DELETE FROM code_description
WHERE code_group = 'main-code'
  AND code_field = 'main-user';
