CREATE TABLE billing_read_model (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    billable_hour_id UUID NOT NULL UNIQUE,
    user_id UUID NOT NULL,
    user_name VARCHAR(100),
    customer_id UUID NOT NULL,
    customer_name VARCHAR(200),
    category_id UUID NOT NULL,
    category_name VARCHAR(100),
    hourly_rate NUMERIC(10, 2),
    hours NUMERIC(5, 2) NOT NULL,
    work_date DATE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_billing_read_model_user_id ON billing_read_model(user_id);
CREATE INDEX idx_billing_read_model_customer_id ON billing_read_model(customer_id);
CREATE INDEX idx_billing_read_model_work_date ON billing_read_model(work_date);
CREATE INDEX idx_billing_read_model_billable_hour_id ON billing_read_model(billable_hour_id);
