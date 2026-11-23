package com.flightbooking;

public class Passenger {

    //Attributs
    private final String passengerId;
    private final String firstName;
    private final String lastName;
    private String email;

    //Constructeur
    public  Passenger(String passengerId, String firstName, String lastName, String email) {
        if (passengerId == null || passengerId.isBlank()) {
            throw new IllegalArgumentException("Passenger ID cannot be null or empty");
        }
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name cannot be null or empty");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name cannot be null or empty");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        this.passengerId = passengerId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
    }
    // Getters
    public String getPassengerId() {
        return passengerId;
    }
    public String getFirstName() {
        return firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public String getEmail() {
        return email;
    }

    // modifier le mail du passager
    public void setEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        this.email = email;
    }

    // toString
    @Override
    public String toString() {
        return "Passenger{" +
                "passengerId='" + passengerId + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                '}';
    }


}


