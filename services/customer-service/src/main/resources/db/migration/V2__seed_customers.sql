-- Seed customers (idempotent)
INSERT INTO customers (id, name, email, address, created_at, updated_at)
VALUES 
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Acme Corp', 'billing@acme.com', '123 Business St', NOW(), NOW()),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'TechStart Inc', 'finance@techstart.com', '456 Innovation Ave', NOW(), NOW()),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'MegaCorp Ltd', 'accounts@megacorp.com', '789 Enterprise Blvd', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
