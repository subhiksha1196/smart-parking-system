package com.parking.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vehicles")
public class Vehicle {
    @Id
    private String licensePlate;
    
    @Enumerated(EnumType.STRING)
    private VehicleType type;
    
    private String ownerContact;
    
    public Vehicle() {}
    
    public Vehicle(String licensePlate, VehicleType type, String ownerContact) {
        this.licensePlate = licensePlate;
        this.type = type;
        this.ownerContact = ownerContact;
    }
    
    // Getters and Setters
    public String getLicensePlate() { return licensePlate; }
    public void setLicensePlate(String licensePlate) { this.licensePlate = licensePlate; }
    
    public VehicleType getType() { return type; }
    public void setType(VehicleType type) { this.type = type; }
    
    public String getOwnerContact() { return ownerContact; }
    public void setOwnerContact(String ownerContact) { this.ownerContact = ownerContact; }
}