-- Simple database schema for our Spring Batch demo
-- This table stores the processed customer data

DROP TABLE IF EXISTS customers;

CREATE TABLE customers (
    id              BIGINT PRIMARY KEY,
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    email           VARCHAR(150),
    country         VARCHAR(50),
    purchase_amount DOUBLE
);
