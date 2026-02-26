-- Seed users (idempotent)
INSERT INTO users (id, name, email, created_at, updated_at)
VALUES 
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'John Doe', 'john.doe@example.com', NOW(), NOW()),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Jane Smith', 'jane.smith@example.com', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
