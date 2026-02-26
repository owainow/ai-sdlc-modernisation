CREATE TABLE IF NOT EXISTS report_users (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS report_customers (
    id UUID PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    email VARCHAR(255),
    address VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS report_billing_categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    hourly_rate DECIMAL(10, 2) NOT NULL
);

CREATE TABLE IF NOT EXISTS report_billable_hours (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL REFERENCES report_customers(id),
    user_id UUID NOT NULL REFERENCES report_users(id),
    category_id UUID NOT NULL REFERENCES report_billing_categories(id),
    hours DECIMAL(5, 2) NOT NULL,
    rate_snapshot DECIMAL(10, 2) NOT NULL,
    date_logged DATE NOT NULL,
    note VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_report_bh_customer ON report_billable_hours(customer_id);
CREATE INDEX idx_report_bh_user ON report_billable_hours(user_id);
CREATE INDEX idx_report_bh_date ON report_billable_hours(date_logged);
