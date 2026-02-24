CREATE TABLE billable_hours (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    category_id UUID NOT NULL REFERENCES billing_categories(id),
    hours NUMERIC(5, 2) NOT NULL CHECK (hours > 0 AND hours <= 24),
    work_date DATE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_billable_hours_user_id ON billable_hours(user_id);
CREATE INDEX idx_billable_hours_customer_id ON billable_hours(customer_id);
CREATE INDEX idx_billable_hours_category_id ON billable_hours(category_id);
CREATE INDEX idx_billable_hours_work_date ON billable_hours(work_date);
