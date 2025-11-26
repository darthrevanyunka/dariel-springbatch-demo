package com.dariel.batchdemo.basics.processing;

import com.dariel.batchdemo.basics.domain.Person;
import org.springframework.batch.item.ItemProcessor;

/**
 * PersonProcessor - OPTIONAL processor that transforms data.
 * 
 * This demonstrates the PROCESS step in Spring Batch.
 * 
 * Key points:
 * - Processor is OPTIONAL - you can have Reader → Writer without a processor
 * - If processor returns null, the item is skipped (not written)
 * - If processor returns a Person, it will be written
 * 
 * In this example, we just uppercase the names to demonstrate transformation.
 */
public class PersonProcessor implements ItemProcessor<Person, Person> {

    @Override
    public Person process(Person person) {
        // Simple transformation: uppercase the names
        Person transformed = new Person();
        transformed.setFirstName(person.getFirstName() != null ? 
                person.getFirstName().toUpperCase() : "");
        transformed.setLastName(person.getLastName() != null ? 
                person.getLastName().toUpperCase() : "");
        
        // Return the transformed person (will be written to output)
        // If we returned null here, this person would be skipped
        return transformed;
    }
}

