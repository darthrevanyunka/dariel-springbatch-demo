# Spring Batch Basics Tutorial

This is the **simplest possible** Spring Batch example to understand the core concepts.

## What This Tutorial Demonstrates

The **core Spring Batch pattern**: **READ → PROCESS → WRITE**

```
input.csv → Reader → Processor → Writer → output.csv
```

## Key Concepts

### 1. READER
- **What it does**: Reads data from a source (CSV file, database, etc.)
- **In this example**: Reads `Person` objects from `input.csv`
- **One line = One Person object**

### 2. PROCESSOR (OPTIONAL!)
- **What it does**: Transforms or validates each item
- **In this example**: Uppercases the names
- **Key point**: Processor is **OPTIONAL** - you can have Reader → Writer without a processor
- **If processor returns `null`**: Item is skipped (not written)
- **If processor returns a Person**: Item will be written

### 3. WRITER
- **What it does**: Writes processed items to a destination (CSV file, database, etc.)
- **In this example**: Writes `Person` objects to `output.csv`
- **Processes in chunks**: Collects 10 items, writes them all at once (efficient!)

## Running the Basics Tutorial

### Step 1: Run the application

By default, the application runs **both** jobs (basics and advanced) sequentially. Simply run:

```bash
mvn spring-boot:run
```

The basics job will run first, followed by the advanced job.

### Step 2: Check the output

Look for `basics-output.csv` in the project root. You should see:
```csv
firstName,lastName
JILL,DOE
JOE,DOE
JUSTIN,DOE
JANE,DOE
JOHN,DOE
```

Notice the names are now UPPERCASE (that's the processor doing its work!)

## File Structure

```
src/main/java/com/dariel/batchdemo/basics/
├── domain/
│   └── Person.java              # Simple domain: firstName, lastName
├── processing/
│   └── PersonProcessor.java     # Transforms names to uppercase
└── config/
    └── BasicsJobConfig.java     # Defines the job, step, reader, processor, writer

src/main/resources/basics/
└── input.csv                    # Input file (5 people)
```

## Understanding the Code

### Person.java
The simplest possible domain object - just two fields.

### PersonProcessor.java
- Implements `ItemProcessor<Person, Person>`
- Takes a Person, transforms it, returns a Person
- If it returns `null`, the item is skipped

### BasicsJobConfig.java
This is where everything is configured:

1. **Job**: The overall batch process
2. **Step**: Defines READ → PROCESS → WRITE
3. **Reader**: Reads from `input.csv`
4. **Processor**: Uppercases names (optional!)
5. **Writer**: Writes to `basics-output.csv`

## Key Takeaways

1. **Reader** = Read from source
2. **Processor** = Transform/validate (OPTIONAL!)
3. **Writer** = Write to destination
4. **Chunk processing** = Process multiple items at once (efficient!)

## Running Just the Basics Job

If you want to run **only** the basics job (without the advanced job), you can modify `SpringBatchDemoApplication.java` to comment out the advanced job execution, or configure `application.yml`:

```yaml
spring:
  batch:
    job:
      names: basicsJob
      enabled: true
```

Then remove or comment out the `CommandLineRunner` bean in `SpringBatchDemoApplication.java`.

## Next Steps

Once you understand this basics tutorial, check out the advanced demo:
- Database integration
- Multiple steps
- Aggregation
- Complex processing

The advanced job (`customerJob`) runs automatically after the basics job, or you can run it separately!

