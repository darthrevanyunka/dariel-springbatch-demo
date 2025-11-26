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

### 1. Switch to basics job

Edit `src/main/resources/application.yml`:
```yaml
spring:
  batch:
    job:
      name: basicsJob  # ← Make sure this is set
      enabled: true
```

### 2. Run it
```bash
mvn spring-boot:run
```

### 3. Check the output

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

## Next: Advanced Demo

Once they understand this, switch to `customerJob` in `application.yml` to see:
- Database integration
- Multiple steps
- Aggregation
- Complex processing

