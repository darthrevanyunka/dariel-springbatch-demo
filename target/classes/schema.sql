-- Simple database schema for our Spring Batch demo
-- This table stores the processed customer data

DROP TABLE IF EXISTS country_statistics;
DROP TABLE IF EXISTS customers;

CREATE TABLE customers (
    id              BIGINT PRIMARY KEY,
    first_name      VARCHAR(100),
    last_name       VARCHAR(100),
    email           VARCHAR(150),
    country         VARCHAR(50),
    purchase_amount DOUBLE
);

-- Table to store aggregated statistics by country
-- This is populated by the second step in our batch job
CREATE TABLE country_statistics (
    country                 VARCHAR(50) PRIMARY KEY,
    customer_count          BIGINT,
    total_revenue           DOUBLE,
    average_purchase_amount DOUBLE
);
