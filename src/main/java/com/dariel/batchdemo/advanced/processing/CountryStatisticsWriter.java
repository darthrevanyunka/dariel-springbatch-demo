package com.dariel.batchdemo.advanced.processing;

import com.dariel.batchdemo.advanced.domain.CountryStatistics;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;

import javax.sql.DataSource;

/**
 * CountryStatisticsWriter - Writes aggregated country statistics to database.
 * 
 * This writer demonstrates:
 * - Writing aggregated/computed data (not just raw input)
 * - Using INSERT with conflict handling (could use MERGE or UPSERT)
 * - Writing statistics that are the result of complex calculations
 */
public class CountryStatisticsWriter {

    /**
     * Creates a writer that inserts country statistics into the database.
     * Uses INSERT with ON CONFLICT handling (if supported) or simple INSERT.
     */
    public static JdbcBatchItemWriter<CountryStatistics> create(DataSource dataSource) {
        // SQL to insert country statistics
        // Note: Using simple INSERT - in production you might want UPSERT logic
        String sql = "INSERT INTO country_statistics(country, customer_count, total_revenue, average_purchase_amount) " +
                     "VALUES (:country, :customerCount, :totalRevenue, :averagePurchaseAmount)";

        return new JdbcBatchItemWriterBuilder<CountryStatistics>()
                .dataSource(dataSource)
                .sql(sql)
                .beanMapped() // Automatically map CountryStatistics properties to SQL placeholders
                .build();
    }
}

