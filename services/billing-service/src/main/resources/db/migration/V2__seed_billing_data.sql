-- Seed billing categories (idempotent)
INSERT INTO billing_categories (id, name, description, hourly_rate, created_at, updated_at)
VALUES 
    ('f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 'Development', 'Software development services', 150.00, NOW(), NOW()),
    ('f1eebc99-9c0b-4ef8-bb6d-6bb9bd380a77', 'Consulting', 'Business consulting services', 200.00, NOW(), NOW()),
    ('f2eebc99-9c0b-4ef8-bb6d-6bb9bd380a88', 'Support', 'Technical support services', 100.00, NOW(), NOW())
ON CONFLICT (id) DO NOTHING;

-- Seed billable hours (6 entries with rateSnapshot)
INSERT INTO billable_hours (id, customer_id, user_id, category_id, hours, rate_snapshot, date_logged, note, created_at, updated_at)
VALUES 
    ('10eebc99-9c0b-4ef8-bb6d-6bb9bd380b01', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 8.00, 150.00, '2025-01-15', 'Backend API development for Acme Corp', NOW(), NOW()),
    ('20eebc99-9c0b-4ef8-bb6d-6bb9bd380b02', 'c0eebc99-9c0b-4ef8-bb6d-6bb9bd380a33', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'f1eebc99-9c0b-4ef8-bb6d-6bb9bd380a77', 4.00, 200.00, '2025-01-16', 'Architecture consulting session', NOW(), NOW()),
    ('30eebc99-9c0b-4ef8-bb6d-6bb9bd380b03', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 6.00, 150.00, '2025-01-17', 'Frontend development for TechStart', NOW(), NOW()),
    ('40eebc99-9c0b-4ef8-bb6d-6bb9bd380b04', 'd0eebc99-9c0b-4ef8-bb6d-6bb9bd380a44', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'f2eebc99-9c0b-4ef8-bb6d-6bb9bd380a88', 3.00, 100.00, '2025-01-18', 'Technical support for TechStart', NOW(), NOW()),
    ('50eebc99-9c0b-4ef8-bb6d-6bb9bd380b05', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', 'f1eebc99-9c0b-4ef8-bb6d-6bb9bd380a77', 5.00, 200.00, '2025-01-20', 'Strategic consulting for MegaCorp', NOW(), NOW()),
    ('60eebc99-9c0b-4ef8-bb6d-6bb9bd380b06', 'e0eebc99-9c0b-4ef8-bb6d-6bb9bd380a55', 'b0eebc99-9c0b-4ef8-bb6d-6bb9bd380a22', 'f0eebc99-9c0b-4ef8-bb6d-6bb9bd380a66', 7.00, 150.00, '2025-01-21', 'Database optimization for MegaCorp', NOW(), NOW())
ON CONFLICT (id) DO NOTHING;
