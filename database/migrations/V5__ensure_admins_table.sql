CREATE TABLE IF NOT EXISTS admins (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE,
    admin_id VARCHAR(100) NOT NULL UNIQUE,
    full_name VARCHAR(150) NOT NULL,
    CONSTRAINT fk_admin_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE
);
