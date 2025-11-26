package com.dariel.batchdemo.processing;

import com.dariel.batchdemo.advanced.domain.CountryStatistics;
import com.dariel.batchdemo.advanced.processing.CountryStatisticsProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for CountryStatisticsProcessor.
 */
class CountryStatisticsProcessorTest {

    private CountryStatisticsProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new CountryStatisticsProcessor();
    }

    @Test
    void process_validStatistics_returnsProcessedStatistics() {
        // Given
        CountryStatistics statistics = new CountryStatistics("USA", 10L, 1234.56789, 123.456789);

        // When
        CountryStatistics result = processor.process(statistics);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCountry()).isEqualTo("USA");
        assertThat(result.getCustomerCount()).isEqualTo(10L);
        assertThat(result.getTotalRevenue()).isEqualTo(1234.57); // Rounded to 2 decimal places
        assertThat(result.getAveragePurchaseAmount()).isEqualTo(123.46); // Rounded to 2 decimal places
    }

    @Test
    void process_tooFewCustomers_returnsNull() {
        // Given - customer count is 0, which is less than MIN_CUSTOMER_COUNT (1)
        CountryStatistics statistics = new CountryStatistics("USA", 0L, 100.0, 100.0);

        // When
        CountryStatistics result = processor.process(statistics);

        // Then
        assertThat(result).isNull(); // Should be filtered out
    }

    @Test
    void process_nullCountry_setsToUnknown() {
        // Given
        CountryStatistics statistics = new CountryStatistics(null, 5L, 500.0, 100.0);

        // When
        CountryStatistics result = processor.process(statistics);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCountry()).isEqualTo("UNKNOWN");
    }

    @Test
    void process_blankCountry_setsToUnknown() {
        // Given
        CountryStatistics statistics = new CountryStatistics("   ", 5L, 500.0, 100.0);

        // When
        CountryStatistics result = processor.process(statistics);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCountry()).isEqualTo("UNKNOWN");
    }

    @Test
    void process_roundsMonetaryValues() {
        // Given
        CountryStatistics statistics = new CountryStatistics("UK", 3L, 99.999, 33.333);

        // When
        CountryStatistics result = processor.process(statistics);

        // Then
        assertThat(result.getTotalRevenue()).isEqualTo(100.0); // Rounded up
        assertThat(result.getAveragePurchaseAmount()).isEqualTo(33.33); // Rounded to 2 decimals
    }
}

