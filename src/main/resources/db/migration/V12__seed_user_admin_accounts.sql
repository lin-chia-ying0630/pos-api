INSERT INTO users (username, password, enabled, created_by, updated_by)
VALUES
    ('user', '$2y$10$6KIjhhEnpPY9lrpY6lCqBuxVNC8Kr2amLuYceXJlC1sWHY3.O8jj2', TRUE, 'system', 'system'),
    ('admin', '$2y$10$qeHiHatyt/sMTkqopL5jVeSm7O1jBDJ.YhsbC8o.lfEyedScKU9Yi', TRUE, 'system', 'system')
ON DUPLICATE KEY UPDATE
    password = VALUES(password),
    enabled = VALUES(enabled),
    updated_by = 'system';

INSERT INTO authorities (username, authority, created_by, updated_by)
VALUES
    ('user', 'ROLE_USER', 'system', 'system'),
    ('admin', 'ROLE_ADMIN', 'system', 'system')
ON DUPLICATE KEY UPDATE
    updated_by = 'system';
