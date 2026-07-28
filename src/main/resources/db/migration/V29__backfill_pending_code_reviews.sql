INSERT INTO change_review (
    operation, source_type, source_record_type, function_code,
    unique_key, review_key, content_before, content_after, review_status,
    created_by, created_at
)
SELECT 'CREATE', 'CODE_TABLE', 'CODE', 'M007',
       CONCAT(code_group, '|', code_field, '|', code_before),
       CONCAT('M007|', code_group, '|', code_field, '|', code_before, '|', code_description),
       NULL,
       CONCAT('{codeGroup=', code_group, ', codeField=', code_field,
              ', codeBefore=', code_before, ', codeAfter=', COALESCE(code_after, ''),
              ', codeDescription=', code_description, '}'),
       'P', COALESCE(created_by, 'system'), COALESCE(created_at, CURRENT_TIMESTAMP)
FROM code_description c
WHERE c.review_status = 'P'
  AND NOT EXISTS (
      SELECT 1 FROM change_review r
      WHERE r.function_code = 'M007'
        AND r.unique_key = CONCAT(c.code_group, '|', c.code_field, '|', c.code_before)
  );
