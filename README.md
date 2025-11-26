# Spring Batch Demo

A comprehensive Spring Batch demonstration with **two tutorials**: a simple basics tutorial and an advanced example showcasing real-world patterns.

## 🎯 Two Tutorials

This project contains two separate demos:

1. **Basics Tutorial** - The simplest possible Spring Batch example (CSV → CSV)
2. **Advanced Demo** - Real-world patterns with database, aggregation, and multiple steps

---

## 📚 Tutorial 1: Basics (Simple CSV → CSV)

**Perfect for beginners!** This demonstrates the core Spring Batch pattern: **READ → PROCESS → WRITE**

### What It Does

```
input.csv → Reader → Processor → Writer → output.csv
```

1. **READ**: Reads `Person` objects from `basics/input.csv`
2. **PROCESS**: Uppercases the names (processor is optional!)
3. **WRITE**: Writes processed data to `basics-output.csv`

### Key Concepts

- **Reader**: Reads data from a source (CSV file)
- **Processor**: Transforms/validates each item (OPTIONAL - can skip it!)
- **Writer**: Writes processed items to a destination
- **Chunk Processing**: Processes items in batches (10 at a time) for efficiency

### Running the Basics Tutorial

**By default, both jobs run automatically!** Simply run:

```bash
mvn spring-boot:run
```

The basics job runs first, followed by the advanced job.

**To run only the basics job**, see the "Running Individual Jobs" section below.

**Check output**: Look for `basics-output.csv` in the project root

### Project Structure

```
src/main/java/com/dariel/batchdemo/basics/
├── domain/
│   └── Person.java              # Simple domain: firstName, lastName
├── processing/
│   └── PersonProcessor.java     # Transforms names to uppercase
└── config/
    └── BasicsJobConfig.java     # Defines job, step, reader, processor, writer

src/main/resources/basics/
└── input.csv                    # Input file (5 people)
```

### Learn More

See [BASICS_TUTORIAL.md](BASICS_TUTORIAL.md) for detailed explanation.

---

## 🚀 Tutorial 2: Advanced (Database & Aggregation)

**Real-world patterns!** This demonstrates:
- Database integration (reading from and writing to database)
- Multiple steps in a job
- Data aggregation
- Complex processing with filtering

### What It Does

**Step 1: CSV → Database**
```
customers.csv → Reader → Processor → Writer → Database
```
- Reads 10,000+ customer records from CSV
- Validates and cleans data (filters invalid emails, negative amounts)
- Writes valid customers to database

**Step 2: Database → Aggregation → CSV**
```
Database → Aggregate Reader → Processor → Writer → country-statistics.csv
```
- Reads all customers from database
- Aggregates by country (count, total revenue, average purchase)
- Writes statistics to CSV file

### Key Concepts

- **Multiple Steps**: A job can have multiple steps that run sequentially
- **Custom Reader**: `CountryStatisticsReader` performs aggregation before returning items
- **Chunk Processing**: Even with aggregation, Spring Batch processes in chunks (10 items at a time)
- **Database Integration**: Using `JdbcCursorItemReader` and `JdbcBatchItemWriter`

### Running the Advanced Demo

**By default, both jobs run automatically!** Simply run:

```bash
mvn spring-boot:run
```

The advanced job runs after the basics job completes.

**To run only the advanced job**, see the "Running Individual Jobs" section below.

**Check output**: Look for `country-statistics.csv` in the project root

### Project Structure

```
src/main/java/com/dariel/batchdemo/advanced/
├── config/
│   └── BatchJobConfig.java          # Defines both steps and the job
├── domain/
│   ├── Customer.java                # Customer data model
│   └── CountryStatistics.java       # Aggregated statistics model
├── processing/
│   ├── CustomerProcessor.java       # Validates and cleans customers
│   ├── CountryStatisticsReader.java # Custom reader with aggregation
│   ├── CountryStatisticsProcessor.java
│   └── CountryStatisticsWriter.java
└── monitoring/
    ├── DemoJobExecutionListener.java    # Job-level logging
    ├── DemoStepExecutionListener.java   # Step-level logging
    └── ChunkLoggingListener.java        # Chunk-level logging

src/main/resources/
├── data/
│   └── customers.csv                # Input CSV (10,000+ records)
└── schema.sql                       # Database table definitions
```

### Example Output

```
🚀 BATCH JOB STARTING
═══════════════════════════════════════════════════════════════════
   Job: customerJob

───────────────────────────────────────────────────────────────────
▶ STEP: processStep
───────────────────────────────────────────────────────────────────
  📦 Processing chunk #1...
  ✓ Chunk #1 completed | Read: 25, Written: 25, Skipped: 0
  ...

───────────────────────────────────────────────────────────────────
▶ STEP: aggregateStep
───────────────────────────────────────────────────────────────────
  📊 Reading all customers from database and aggregating by country...
  ✓ Read 9197 customers, aggregated into 41 countries
  📦 Processing chunk #1...
  💾 Writer.write() called with 10 items (chunk size reached!)
  ...

✅ BATCH JOB COMPLETED
═══════════════════════════════════════════════════════════════════
   Duration: 671 ms

📊 SUMMARY STATISTICS
───────────────────────────────────────────────────────────────────
   Total Steps: 2

   📋 Step: processStep
      • Read:         10,001 items
      • Written:       9,197 items
      • Skipped:           0 items
      • Filtered:        804 items
      • Duration:        639 ms

   📋 Step: aggregateStep
      • Read:             41 items
      • Written:          41 items
      • Skipped:           0 items
      • Filtered:          0 items
      • Duration:         25 ms
```

---

## 🎓 Key Spring Batch Concepts

### The Core Pattern: READ → PROCESS → WRITE

```
Source → Reader → Processor → Writer → Destination
```

- **Reader**: Reads items from a source (CSV, database, etc.)
- **Processor**: Transforms/validates items (OPTIONAL - can return `null` to skip)
- **Writer**: Writes items to a destination (CSV, database, etc.)

### Chunk Processing

Spring Batch processes items in **chunks** (batches) for efficiency:
- Reads multiple items (chunk size)
- Processes them
- Writes them all at once
- Much faster than one-at-a-time processing!

### Jobs and Steps

- **Job**: The overall batch process (can have multiple steps)
- **Step**: A single READ → PROCESS → WRITE operation
- Steps run sequentially within a job

---

## 🚀 Running the Application

### Default Behavior (Both Jobs)

By default, **both jobs run automatically** when you start the application:

```bash
mvn spring-boot:run
```

This runs:
1. `basicsJob` - Simple CSV → CSV example
2. `customerJob` - Advanced database & aggregation example

### Running Individual Jobs

To run **only one job**, you have two options:

**Option 1: Modify the CommandLineRunner** (in `SpringBatchDemoApplication.java`)
- Comment out the job you don't want to run

**Option 2: Use application.yml configuration**
- Set `spring.batch.job.enabled: false` in `application.yml`
- Configure which job to run:
  ```yaml
  spring:
    batch:
      job:
        names: basicsJob  # or customerJob, or basicsJob,customerJob
        enabled: true
  ```
- Remove or comment out the `CommandLineRunner` bean in `SpringBatchDemoApplication.java`

---

## 📁 Project Structure

```
src/main/java/com/dariel/batchdemo/
├── basics/                          # Simple CSV → CSV tutorial
│   ├── config/
│   ├── domain/
│   └── processing/
├── advanced/                        # Advanced database & aggregation demo
│   ├── config/
│   ├── domain/
│   ├── processing/
│   └── monitoring/
└── SpringBatchDemoApplication.java  # Main application

src/main/resources/
├── basics/
│   └── input.csv                   # Basics tutorial input
├── data/
│   └── customers.csv               # Advanced demo input (10k+ records)
├── application.yml                 # Configuration
└── schema.sql                      # Database schema
```

---

## 🛠️ Prerequisites

- Java 17+
- Maven 3.6+

## 📦 Running the Application

```bash
# Run tests
mvn test

# Run the application
mvn spring-boot:run
```

## 📚 Documentation

- [BASICS_TUTORIAL.md](BASICS_TUTORIAL.md) - Detailed basics tutorial guide
- [BASICS_QUICK_START.md](BASICS_QUICK_START.md) - Quick start for basics
- `docs/` - Visual diagrams of Spring Batch architecture

## 🎯 Learning Path

1. **Start with Basics**: Understand READ → PROCESS → WRITE with simple CSV files
2. **Move to Advanced**: See real-world patterns with database and aggregation
3. **Experiment**: Modify chunk sizes, add processors, change data sources

## 💡 Key Takeaways

1. **Reader** = Read from source
2. **Processor** = Transform/validate (OPTIONAL!)
3. **Writer** = Write to destination
4. **Chunk processing** = Process multiple items at once (efficient!)
5. **Jobs can have multiple steps** = Chain operations together
6. **Custom readers** = Perform complex operations like aggregation

