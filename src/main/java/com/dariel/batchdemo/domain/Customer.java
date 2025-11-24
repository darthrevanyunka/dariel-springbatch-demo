package com.dariel.batchdemo.domain;

/**
 * Simple Customer domain object representing a customer record.
 * This is what we read from CSV, process, and write to the database.
 */
public class Customer {

    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String country;
    private Double purchaseAmount;

    // Default constructor required by Spring Batch for bean mapping
    public Customer() {
    }

    public Customer(Long id, String firstName, String lastName, String email, String country, Double purchaseAmount) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.country = country;
        this.purchaseAmount = purchaseAmount;
    }

    // Getters and setters required for Spring Batch bean mapping
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Double getPurchaseAmount() {
        return purchaseAmount;
    }

    public void setPurchaseAmount(Double purchaseAmount) {
        this.purchaseAmount = purchaseAmount;
    }

    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", country='" + country + '\'' +
                ", purchaseAmount=" + purchaseAmount +
                '}';
    }
}

