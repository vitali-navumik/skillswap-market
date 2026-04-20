CREATE TABLE app_users
(
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(320)             NOT NULL,
    password_hash VARCHAR(255)             NOT NULL,
    first_name    VARCHAR(100)             NOT NULL,
    last_name     VARCHAR(100)             NOT NULL,
    display_name  VARCHAR(150),
    status        VARCHAR(32)              NOT NULL,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_app_users_email UNIQUE (email)
);

CREATE TABLE user_roles
(
    user_id BIGINT      NOT NULL,
    role    VARCHAR(32) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES app_users (id) ON DELETE CASCADE
);

CREATE INDEX idx_app_users_status ON app_users (status);
CREATE INDEX idx_user_roles_role ON user_roles (role);
