package com.dariel.batchdemo.basics.domain;

/**
 * Person - Simple domain object for the basics tutorial.
 * 
 * This is the simplest possible example - just a first name and last name.
 * No validation, no complex fields - just the basics.
 */
public class Person {

    private String firstName;
    private String lastName;

    // Default constructor required by Spring Batch
    public Person() {
    }

    public Person(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
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

    @Override
    public String toString() {
        return "Person{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                '}';
    }
}

