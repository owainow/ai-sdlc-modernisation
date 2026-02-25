CREATE TABLE billing_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) NOT NULL UNIQUE,
    hourly_rate NUMERIC(10, 2) NOT NULL CHECK (hourly_rate > 0 AND hourly_rate <= 10000),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_billing_categories_name ON billing_categories(name);
