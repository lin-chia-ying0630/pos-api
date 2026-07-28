DELETE FROM code_description
WHERE code_group = 'main-screen';

DELETE FROM code_description
WHERE code_group = 'main-code'
  AND code_field = 'main-screen';
