package com.dariel.batchdemo.advanced.processing;

import com.dariel.batchdemo.advanced.domain.CountryStatistics;
import com.dariel.batchdemo.advanced.domain.Customer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.jdbc.core.BeanPropertyRowMapper;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * CountryStatisticsReader - Advanced reader that aggregates customer data by country.
 * 
 * This demonstrates a more complex reader pattern:
 * 1. Reads all customers from the database
 * 2. Groups them by country
 * 3. Calculates statistics (count, total revenue, average purchase)
 * 4. Returns CountryStatistics objects for each country
 * 
 * This is more advanced than a simple CSV reader because it:
 * - Reads from a database (not a file)
 * - Performs aggregation logic
 * - Transforms data structure (Customer -> CountryStatistics)
 */
public class CountryStatisticsReader implements ItemStreamReader<CountryStatistics> {

    private final DataSource dataSource;
    private JdbcCursorItemReader<Customer> customerReader;
    private Iterator<CountryStatistics> statisticsIterator;
    private List<CountryStatistics> statisticsList;

    public CountryStatisticsReader(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public CountryStatistics read() throws Exception {
        if (statisticsIterator == null) {
            aggregateData();
        }

        if (statisticsIterator != null && statisticsIterator.hasNext()) {
            return statisticsIterator.next();
        }

        return null; // No more items
    }

    @Override
    public void open(ExecutionContext executionContext) throws ItemStreamException {
        // Initialize the customer reader
        customerReader = new JdbcCursorItemReaderBuilder<Customer>()
                .name("customerReader")
                .dataSource(dataSource)
                .sql("SELECT id, first_name, last_name, email, country, purchase_amount FROM customers ORDER BY country")
                .rowMapper(new BeanPropertyRowMapper<Customer>() {
                    @Override
                    public Customer mapRow(ResultSet rs, int rowNum) throws SQLException {
                        Customer customer = new Customer();
                        customer.setId(rs.getLong("id"));
                        customer.setFirstName(rs.getString("first_name"));
                        customer.setLastName(rs.getString("last_name"));
                        customer.setEmail(rs.getString("email"));
                        customer.setCountry(rs.getString("country"));
                        customer.setPurchaseAmount(rs.getDouble("purchase_amount"));
                        return customer;
                    }
                })
                .build();
        
        customerReader.open(executionContext);
    }

    @Override
    public void update(ExecutionContext executionContext) throws ItemStreamException {
        if (customerReader != null) {
            customerReader.update(executionContext);
        }
    }

    @Override
    public void close() throws ItemStreamException {
        if (customerReader != null) {
            customerReader.close();
        }
    }

    /**
     * Read all customers and aggregate them by country
     * 
     * IMPORTANT: This reads ALL customers from the database first, then aggregates them.
     * Spring Batch only sees the final CountryStatistics objects (one per country),
     * not the individual customers being read.
     */
    private void aggregateData() throws Exception {
        System.out.println("  📊 Reading all customers from database and aggregating by country...");
        
        // Map to store aggregated data by country
        Map<String, CountryAggregation> aggregations = new HashMap<>();

        int customerCount = 0;
        Customer customer;
        while ((customer = customerReader.read()) != null) {
            customerCount++;
            String country = customer.getCountry() != null ? customer.getCountry() : "UNKNOWN";
            
            aggregations.computeIfAbsent(country, k -> new CountryAggregation(country))
                    .addCustomer(customer.getPurchaseAmount());
        }

        System.out.printf("  ✓ Read %d customers, aggregated into %d countries%n", 
                customerCount, aggregations.size());

        // Convert aggregations to CountryStatistics objects
        statisticsList = new ArrayList<>();
        for (CountryAggregation aggregation : aggregations.values()) {
            statisticsList.add(aggregation.toStatistics());
        }
        
        statisticsIterator = statisticsList.iterator();
    }

    /**
     * Helper class to aggregate customer data for a country
     */
    private static class CountryAggregation {
        private final String country;
        private long count = 0;
        private double totalRevenue = 0.0;

        public CountryAggregation(String country) {
            this.country = country;
        }

        public void addCustomer(Double purchaseAmount) {
            count++;
            if (purchaseAmount != null) {
                totalRevenue += purchaseAmount;
            }
        }

        public CountryStatistics toStatistics() {
            double average = count > 0 ? totalRevenue / count : 0.0;
            return new CountryStatistics(country, count, totalRevenue, average);
        }
    }
}

