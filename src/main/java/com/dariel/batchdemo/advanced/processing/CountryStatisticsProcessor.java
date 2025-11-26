package com.dariel.batchdemo.advanced.processing;

import com.dariel.batchdemo.advanced.domain.CountryStatistics;
import org.springframework.batch.item.ItemProcessor;

/**
 * CountryStatisticsProcessor - Processes aggregated country statistics.
 * 
 * This processor can:
 * - Filter out countries with too few customers
 * - Round monetary values for reporting
 * - Apply business rules (e.g., minimum revenue thresholds)
 * 
 * This demonstrates advanced processing logic on aggregated data.
 */
public class CountryStatisticsProcessor implements ItemProcessor<CountryStatistics, CountryStatistics> {

    // Minimum number of customers required to include a country in statistics
    private static final int MIN_CUSTOMER_COUNT = 1;
    
    // Round to 2 decimal places for currency
    private static final int DECIMAL_PLACES = 2;

    @Override
    public CountryStatistics process(CountryStatistics statistics) {
        // Filter: Skip countries with too few customers
        if (statistics.getCustomerCount() < MIN_CUSTOMER_COUNT) {
            return null; // Skip this country
        }

        // Round monetary values for cleaner reporting
        if (statistics.getTotalRevenue() != null) {
            statistics.setTotalRevenue(round(statistics.getTotalRevenue()));
        }
        
        if (statistics.getAveragePurchaseAmount() != null) {
            statistics.setAveragePurchaseAmount(round(statistics.getAveragePurchaseAmount()));
        }

        // Ensure country name is not null
        if (statistics.getCountry() == null || statistics.getCountry().isBlank()) {
            statistics.setCountry("UNKNOWN");
        }

        return statistics;
    }

    /**
     * Round a double value to specified decimal places
     */
    private double round(double value) {
        double multiplier = Math.pow(10, DECIMAL_PLACES);
        return Math.round(value * multiplier) / multiplier;
    }
}

