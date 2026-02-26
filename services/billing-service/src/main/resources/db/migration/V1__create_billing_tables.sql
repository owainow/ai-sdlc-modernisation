CREATE TABLE IF NOT EXISTS billing_categories (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    hourly_rate DECIMAL(10, 2) NOT NULL CHECK (hourly_rate > 0),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS billable_hours (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    user_id UUID NOT NULL,
    category_id UUID NOT NULL REFERENCES billing_categories(id),
    hours DECIMAL(5, 2) NOT NULL CHECK (hours > 0 AND hours <= 24),
    rate_snapshot DECIMAL(10, 2) NOT NULL,
    date_logged DATE NOT NULL,
    note VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_billable_hours_customer_id ON billable_hours(customer_id);
CREATE INDEX idx_billable_hours_user_id ON billable_hours(user_id);
CREATE INDEX idx_billable_hours_category_id ON billable_hours(category_id);
CREATE INDEX idx_billable_hours_date_logged ON billable_hours(date_logged);
CREATE INDEX idx_billable_hours_user_date ON billable_hours(user_id, date_logged);
