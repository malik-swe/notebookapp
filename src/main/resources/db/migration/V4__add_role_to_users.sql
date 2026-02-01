-- Add role column to users table
ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'USER';

-- Optional: Create an admin user for testing
-- This is a bcrypt hash of "admin123" with strength 12
INSERT INTO users (username, email, password, role)
VALUES ('admin', 'admin@example.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYq/K8I4uii', 'ADMIN')
    ON CONFLICT (email) DO NOTHING;