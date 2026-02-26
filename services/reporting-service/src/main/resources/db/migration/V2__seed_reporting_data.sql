-- Seed reporting read models (mirrors of other services' data, idempotent)
INSERT INTO report_users (id, name, email)
VALUES 
    ('a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'John Doe', 'john.doe@example.com'),
    ('b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'Jane Smith', 'jane.smith@example.com')
ON CONFLICT (id) DO NOTHING;

INSERT INTO report_customers (id, name, email, address)
VALUES 
    ('c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'Acme Corp', 'billing@acme.com', '123 Business St'),
    ('d0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'TechStart Inc', 'finance@techstart.com', '456 Innovation Ave'),
    ('e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'MegaCorp Ltd', 'accounts@megacorp.com', '789 Enterprise Blvd')
ON CONFLICT (id) DO NOTHING;

INSERT INTO report_billing_categories (id, name, hourly_rate)
VALUES 
    ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'Development', 150.00),
    ('f1eebc99-9c0b-4ef8-bb6d-6bb9bd380a77', 'Consulting', 200.00),
    ('f2eebc99-9c0b-4ef8-bb6d-6bb9bd380a88', 'Support', 100.00)
ON CONFLICT (id) DO NOTHING;

INSERT INTO report_billable_hours (id, customer_id, user_id, category_id, hours, rate_snapshot, date_logged, note, created_at)
VALUES 
    ('10eebc99-9c0b-4ef8-bb6d-6bb9bd380b01', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 8.00, 150.00, '2025-01-15', 'Backend API development for Acme Corp', NOW()),
    ('20eebc99-9c0b-4ef8-bb6d-6bb9bd380b02', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'f1eebc99-9c0b-4ef8-bb6d-6bb9bd380a77', 4.00, 200.00, '2025-01-16', 'Architecture consulting session', NOW()),
    ('30eebc99-9c0b-4ef8-bb6d-6bb9bd380b03', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 6.00, 150.00, '2025-01-17', 'Frontend development for TechStart', NOW()),
    ('40eebc99-9c0b-4ef8-bb6d-6bb9bd380b04', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'f2eebc99-9c0b-4ef8-bb6d-6bb9bd380a88', 3.00, 100.00, '2025-01-18', 'Technical support for TechStart', NOW()),
    ('50eebc99-9c0b-4ef8-bb6d-6bb9bd380b05', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f1eebc99-9c0b-4ef8-bb6d-6bb9bd380a77', 5.00, 200.00, '2025-01-20', 'Strategic consulting for MegaCorp', NOW()),
    ('60eebc99-9c0b-4ef8-bb6d-6bb9bd380b06', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 7.00, 150.00, '2025-01-21', 'Database optimization for MegaCorp', NOW())
ON CONFLICT (id) DO NOTHING;
