CREATE TABLE IF NOT EXISTS user_screen_authorization (
    username VARCHAR(128) NOT NULL,
    function_code VARCHAR(16) NOT NULL,
    created_by VARCHAR(128) NOT NULL DEFAULT 'system',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(128) NOT NULL DEFAULT 'system',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (username, function_code),
    CONSTRAINT fk_user_screen_authorization_user FOREIGN KEY (username) REFERENCES users (username)
);

INSERT IGNORE INTO user_screen_authorization (username, function_code, created_by, updated_by)
SELECT username, function_code, 'system', 'system'
FROM (
    SELECT username, 'MPS00001' function_code FROM authorities WHERE authority = 'ROLE_MAKER'
    UNION SELECT username, 'MPS00002' FROM authorities WHERE authority IN ('ROLE_MAKER', 'ROLE_REVIEWER')
    UNION SELECT username, 'MPM00001' FROM authorities WHERE authority IN ('ROLE_MAKER', 'ROLE_REVIEWER')
    UNION SELECT username, 'MPM00002' FROM authorities WHERE authority IN ('ROLE_MAKER', 'ROLE_REVIEWER')
    UNION SELECT username, 'MPM00003' FROM authorities WHERE authority IN ('ROLE_MAKER', 'ROLE_REVIEWER')
    UNION SELECT username, 'MCM00001' FROM authorities WHERE authority IN ('ROLE_MAKER', 'ROLE_REVIEWER')
    UNION SELECT username, 'MPS00003' FROM authorities WHERE authority = 'ROLE_REVIEWER'
    UNION SELECT username, 'MUS00001' FROM authorities WHERE authority IN ('ROLE_USER', 'ROLE_ADMIN')
) seeded_permissions;
