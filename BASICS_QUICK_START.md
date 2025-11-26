# Spring Batch Basics - Quick Start Guide

## What You'll Learn

The **3 core components** of Spring Batch:
1. **READER** - Reads data from a source
2. **PROCESSOR** - Transforms/validates data (OPTIONAL!)
3. **WRITER** - Writes data to a destination

## The Flow

```
input.csv → READER → PROCESSOR → WRITER → output.csv
```

## Running the Basics Tutorial

### 1. Run it

By default, the application runs **both** jobs (basics and advanced) sequentially:

```bash
mvn spring-boot:run
```

The basics job will run first, followed by the advanced job.

### 2. Check the output

Look for `basics-output.csv` in the project root. You'll see:
- Input: `Jill,Doe` → Output: `JILL,DOE` (names are uppercased!)

## The Code Structure

### Person.java
Simple domain object - just `firstName` and `lastName`.

### PersonProcessor.java
- Takes a Person
- Uppercases the names
- Returns the transformed Person
- **If it returned `null`, the item would be skipped**

### BasicsJobConfig.java
This is where everything is configured:

```java
@Bean
public Step basicsStep(...) {
    return new StepBuilder("basicsStep", jobRepository)
        .<Person, Person>chunk(10, transactionManager)
        .reader(personReader)      // READ from input.csv
        .processor(personProcessor) // PROCESS (uppercase names)
        .writer(personWriter)       // WRITE to output.csv
        .build();
}
```

## Key Points to Explain

1. **Reader** reads from `input.csv` → creates Person objects
2. **Processor** transforms each Person (uppercases names) - **this is optional!**
3. **Writer** writes Person objects to `output.csv`
4. **Chunk processing**: Processes 10 items at a time (efficient!)

## Try This

1. **Remove the processor**: Comment out `.processor(personProcessor)` - output will be lowercase
2. **Change chunk size**: Change `chunk(10, ...)` to `chunk(5, ...)` - processes 5 at a time
3. **Skip items**: Make processor return `null` for some items - they won't be written

## Running Just the Basics Job

To run **only** the basics job, you can:
1. Modify `SpringBatchDemoApplication.java` to comment out the advanced job execution, OR
2. Configure `application.yml`:
   ```yaml
   spring:
     batch:
       job:
         names: basicsJob
         enabled: true
   ```
   Then remove/comment the `CommandLineRunner` bean.

## Next: Advanced Demo

Once they understand this, the advanced job (`customerJob`) runs automatically after the basics job! It demonstrates:
- Database integration
- Multiple steps
- Aggregation
- Complex processing

