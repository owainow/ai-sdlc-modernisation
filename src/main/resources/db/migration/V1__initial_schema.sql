-- V1__initial_schema.sql
-- Flyway migration: Initial schema matching existing Derby database

CREATE TABLE users (
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE customers (
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE billing_categories (
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    name VARCHAR(255) NOT NULL,
    description VARCHAR(500),
    hourly_rate DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE billable_hours (
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    customer_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    hours DECIMAL(8,2) NOT NULL,
    note VARCHAR(1000),
    date_logged DATE NOT NULL,
    created_at TIMESTAMP NOT NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (customer_id) REFERENCES customers(id),
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (category_id) REFERENCES billing_categories(id)
);
