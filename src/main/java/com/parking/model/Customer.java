package com.parking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "customers")
public class Customer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, unique = true)
    private String contact;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private boolean handicapped;
    
    @Column(nullable = false)
    private int loyaltyPoints;
    
    @Column(nullable = false)
    private LocalDateTime registeredAt;
    
    // Constructors
    public Customer() {
    }
    
    public Customer(String name, String contact, String email, boolean handicapped) {
        this.name = name;
        this.contact = contact;
        this.email = email;
        this.handicapped = handicapped;
        this.loyaltyPoints = 0;
        this.registeredAt = LocalDateTime.now();
    }
    
    // Business Logic Methods
    public void addLoyaltyPoints(int points) {
        this.loyaltyPoints += points;
    }
    
    public boolean hasLoyaltyDiscount() {
        return this.loyaltyPoints >= 100;
    }
    
    public void redeemLoyaltyPoints(int points) {
        if (this.loyaltyPoints >= points) {
            this.loyaltyPoints -= points;
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getContact() {
        return contact;
    }
    
    public void setContact(String contact) {
        this.contact = contact;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public boolean isHandicapped() {
        return handicapped;
    }
    
    public void setHandicapped(boolean handicapped) {
        this.handicapped = handicapped;
    }
    
    public int getLoyaltyPoints() {
        return loyaltyPoints;
    }
    
    public void setLoyaltyPoints(int loyaltyPoints) {
        this.loyaltyPoints = loyaltyPoints;
    }
    
    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }
    
    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
    
    @Override
    public String toString() {
        return "Customer{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", contact='" + contact + '\'' +
                ", email='" + email + '\'' +
                ", handicapped=" + handicapped +
                ", loyaltyPoints=" + loyaltyPoints +
                '}';
    }
}