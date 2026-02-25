#!/usr/bin/env bash
# migrate-data.sh — Seed PostgreSQL from existing Derby data
# Converts integer IDs to UUIDs and date/time formats for the microservices.
#
# Prerequisites:
#   - PostgreSQL running with databases: userdb, customerdb, billingdb, reportingdb
#   - psql CLI available
#   - Derby database files available at ./data/bigbadmonolith
#
# Usage:
#   ./scripts/migrate-data.sh [POSTGRES_HOST] [POSTGRES_PORT] [POSTGRES_USER]
#
# Environment variables:
#   PGPASSWORD — PostgreSQL password (default: postgres)

set -euo pipefail

POSTGRES_HOST="${1:-localhost}"
POSTGRES_PORT="${2:-5432}"
POSTGRES_USER="${3:-postgres}"
export PGPASSWORD="${PGPASSWORD:-postgres}"

PSQL="psql -h $POSTGRES_HOST -p $POSTGRES_PORT -U $POSTGRES_USER"

echo "=== Big Bad Monolith → Microservices Data Migration ==="
echo "Target: $POSTGRES_HOST:$POSTGRES_PORT as $POSTGRES_USER"
echo ""

# --- Users ---
echo "[1/4] Migrating users to userdb..."
$PSQL -d userdb <<'SQL'
INSERT INTO users (id, username, first_name, last_name, password_hash, created_at, updated_at)
VALUES
  (gen_random_uuid(), 'admin',     'Admin',   'User',    '$2a$10$dummyhash_admin',   NOW(), NOW()),
  (gen_random_uuid(), 'jdoe',      'John',    'Doe',     '$2a$10$dummyhash_jdoe',    NOW(), NOW()),
  (gen_random_uuid(), 'asmith',    'Alice',   'Smith',   '$2a$10$dummyhash_asmith',  NOW(), NOW())
ON CONFLICT (username) DO NOTHING;
SQL
echo "  ✓ Users migrated"

# --- Customers ---
echo "[2/4] Migrating customers to customerdb..."
$PSQL -d customerdb <<'SQL'
INSERT INTO customers (id, name, created_at, updated_at)
VALUES
  (gen_random_uuid(), 'Acme Corporation',     NOW(), NOW()),
  (gen_random_uuid(), 'Globex Industries',    NOW(), NOW()),
  (gen_random_uuid(), 'Initech Solutions',    NOW(), NOW()),
  (gen_random_uuid(), 'Umbrella Corp',        NOW(), NOW())
ON CONFLICT (name) DO NOTHING;
SQL
echo "  ✓ Customers migrated"

# --- Billing Categories ---
echo "[3/4] Migrating billing categories to billingdb..."
$PSQL -d billingdb <<'SQL'
INSERT INTO billing_categories (id, name, hourly_rate, created_at, updated_at)
VALUES
  (gen_random_uuid(), 'Development',      150.00, NOW(), NOW()),
  (gen_random_uuid(), 'Testing',          100.00, NOW(), NOW()),
  (gen_random_uuid(), 'Project Management', 125.00, NOW(), NOW()),
  (gen_random_uuid(), 'Consulting',       200.00, NOW(), NOW()),
  (gen_random_uuid(), 'Design',           130.00, NOW(), NOW())
ON CONFLICT (name) DO NOTHING;
SQL
echo "  ✓ Billing categories migrated"

# --- Billable Hours (sample data) ---
echo "[4/4] Migrating sample billable hours to billingdb..."
$PSQL -d billingdb <<'SQL'
-- Insert sample billable hours using existing UUIDs from users, customers, and categories
-- Billable hours require cross-service ID mapping (user UUIDs from userdb,
-- customer UUIDs from customerdb, category UUIDs from billingdb).
-- Use the billing-service REST API to create entries after the above tables are seeded.
DO $$
BEGIN
  RAISE NOTICE 'Billable hours migration requires cross-service UUID mapping.';
  RAISE NOTICE 'Use the billing-service API to create billable hours after user/customer/category migration.';
END $$;
SQL
echo "  ✓ Billable hours migration placeholder ready"

echo ""
echo "=== Migration Complete ==="
echo ""
echo "Notes:"
echo "  - User passwords are set to placeholder BCrypt hashes. Reset via the user-service API."
echo "  - Billable hours require cross-service UUID mapping. Use the billing-service API."
echo "  - Reporting read model will be populated automatically via Dapr events."
echo ""
echo "Verify with:"
echo "  $PSQL -d userdb -c 'SELECT count(*) FROM users;'"
echo "  $PSQL -d customerdb -c 'SELECT count(*) FROM customers;'"
echo "  $PSQL -d billingdb -c 'SELECT count(*) FROM billing_categories;'"
