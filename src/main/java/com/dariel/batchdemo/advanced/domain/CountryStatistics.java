package com.dariel.batchdemo.advanced.domain;

/**
 * CountryStatistics - Represents aggregated statistics for a country.
 * This is the output of our aggregation step that processes customer data.
 * 
 * Contains:
 * - Country name
 * - Total number of customers
 * - Total revenue (sum of all purchases)
 * - Average purchase amount
 */
public class CountryStatistics {

    private String country;
    private Long customerCount;
    private Double totalRevenue;
    private Double averagePurchaseAmount;

    // Default constructor required by Spring Batch
    public CountryStatistics() {
    }

    public CountryStatistics(String country, Long customerCount, Double totalRevenue, Double averagePurchaseAmount) {
        this.country = country;
        this.customerCount = customerCount;
        this.totalRevenue = totalRevenue;
        this.averagePurchaseAmount = averagePurchaseAmount;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Long getCustomerCount() {
        return customerCount;
    }

    public void setCustomerCount(Long customerCount) {
        this.customerCount = customerCount;
    }

    public Double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(Double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public Double getAveragePurchaseAmount() {
        return averagePurchaseAmount;
    }

    public void setAveragePurchaseAmount(Double averagePurchaseAmount) {
        this.averagePurchaseAmount = averagePurchaseAmount;
    }

    @Override
    public String toString() {
        return "CountryStatistics{" +
                "country='" + country + '\'' +
                ", customerCount=" + customerCount +
                ", totalRevenue=" + totalRevenue +
                ", averagePurchaseAmount=" + averagePurchaseAmount +
                '}';
    }
}

