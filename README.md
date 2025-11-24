# Spring Batch Simple Demo

A simple Spring Batch application to demonstrate the core concepts: **READ → PROCESS → WRITE**.

## What This Demo Does

1. **READ**: Reads customer data from a CSV file (`customers.csv`)
2. **PROCESS**: Validates and cleans the data (capitalizes names, uppercases countries, filters invalid records)
3. **WRITE**: Writes valid customers to a database table

## Key Concepts

### Spring Batch Pattern

```
CSV File → Reader → Processor → Writer → Database
```

- **Reader**: Reads data from a source (CSV, database, etc.)
- **Processor**: Transforms/validates each item (can return `null` to skip items)
- **Writer**: Writes processed items to a destination (database, file, etc.)

### Chunk Processing

Items are processed in **chunks** (batches) for efficiency:
- Chunk size: 25 items
- Spring Batch reads 25 items, processes them, then writes all 25 to the database at once
- This is much faster than processing one item at a time!

## Project Structure

```
src/main/java/com/dariel/batchdemo/
├── config/
│   └── BatchJobConfig.java          # Main configuration - defines the job
├── domain/
│   └── Customer.java                # Simple Customer data model
├── processing/
│   └── CustomerProcessor.java       # Validates and cleans customer data
└── monitoring/
    └── ChunkLoggingListener.java    # Logs progress for each chunk

src/main/resources/
├── data/
│   └── customers.csv                # Input CSV file (15 rows)
└── schema.sql                       # Database table definition
```

## Running the Application

### Run Tests
```bash
mvn test
```

### Run the Application
```bash
mvn spring-boot:run
```

The batch job will automatically run on startup and process the CSV file.

## Understanding the Code

### 1. BatchJobConfig.java
This is where everything is configured:
- Defines the **Job** (the overall batch process)
- Defines the **Step** (READ → PROCESS → WRITE)
- Configures the **Reader**, **Processor**, and **Writer**

### 2. CustomerProcessor.java
This is where the business logic lives:
- Validates email addresses (must contain "@")
- Validates purchase amounts (must be positive)
- Capitalizes names
- Uppercases countries
- Returns `null` to skip invalid records

### 3. Customer.java
Simple data model with:
- id, firstName, lastName, email, country, purchaseAmount
- Getters and setters (required for Spring Batch)

## Example Output

When you run the application, you'll see logs like:

```
[processStep] Starting chunk #1
[processStep] Completed chunk #1 | read=15, written=13, skipped=0
```

This means:
- Read 15 rows from CSV
- Wrote 13 valid customers to database
- 2 records were invalid and skipped (bad email or negative amount)

## CSV Data

The `customers.csv` file contains 15 rows:
- 13 valid customers (will be written to database)
- 2 invalid customers (row 7: bad email, row 9: negative amount)

## Database

Uses H2 in-memory database. After running, you can check the results:
- Table: `customers`
- Contains 13 valid customer records
- Names are capitalized, countries are uppercased

## Next Steps

Try modifying:
1. **Chunk size** in `BatchJobConfig.java` (try 5 or 50)
2. **Validation rules** in `CustomerProcessor.java`
3. **CSV data** in `customers.csv`
4. **Database schema** in `schema.sql`

## Diagram

See `docs/spring-batch-simple.d2` for a visual diagram of how the batch job works.

