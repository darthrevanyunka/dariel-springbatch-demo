package com.dariel.batchdemo.processing;

import com.dariel.batchdemo.domain.Customer;
import org.springframework.batch.item.ItemProcessor;

/**
 * CustomerProcessor - This is the TRANSFORM step in our ETL pipeline.
 * 
 * Spring Batch calls this for each item read from the CSV file.
 * 
 * What it does:
 * 1. Validates the customer data (filters out invalid records)
 * 2. Cleans/normalizes the data (capitalizes names, uppercases country)
 * 
 * If this returns null, the item is skipped (not written to database).
 * If this returns a Customer, it will be written to the database.
 */
public class CustomerProcessor implements ItemProcessor<Customer, Customer> {

    @Override
    public Customer process(Customer customer) {
        // Step 1: Validate - filter out invalid records
        // If email is missing or invalid, skip this record
        if (customer.getEmail() == null || !customer.getEmail().contains("@")) {
            return null; // Returning null means "skip this record"
        }
        
        // If purchase amount is missing or negative, skip this record
        if (customer.getPurchaseAmount() == null || customer.getPurchaseAmount() <= 0) {
            return null; // Skip invalid records
        }

        // Step 2: Clean and normalize the data
        // Capitalize first letter of names
        customer.setFirstName(capitalize(customer.getFirstName()));
        customer.setLastName(capitalize(customer.getLastName()));
        
        // Uppercase country for consistency
        if (customer.getCountry() != null) {
            customer.setCountry(customer.getCountry().toUpperCase());
        } else {
            customer.setCountry("UNKNOWN");
        }

        // Return the cleaned customer (will be written to database)
        return customer;
    }

    /**
     * Helper method to capitalize the first letter of a string.
     * Example: "john" -> "John"
     */
    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return "UNKNOWN";
        }
        String trimmed = value.trim().toLowerCase();
        return Character.toUpperCase(trimmed.charAt(0)) + trimmed.substring(1);
    }
}

