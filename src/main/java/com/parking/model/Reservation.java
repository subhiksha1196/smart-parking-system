package com.parking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
public class Reservation {
    
    @Id
    private String reservationId;
    
    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;
    
    @ManyToOne
    @JoinColumn(name = "space_id", nullable = false)
    private ParkingSpace reservedSpace;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VehicleType vehicleType;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private int validityHours;
    
    @Column(nullable = false)
    private boolean used;
    
    // Constructors
    public Reservation() {
    }
    
    public Reservation(String reservationId, Customer customer, ParkingSpace reservedSpace, 
                      VehicleType vehicleType, int validityHours) {
        this.reservationId = reservationId;
        this.customer = customer;
        this.reservedSpace = reservedSpace;
        this.vehicleType = vehicleType;
        this.validityHours = validityHours;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = this.createdAt.plusHours(validityHours);
        this.used = false;
    }
    
    // Business Logic Methods
    public boolean isValid() {
        return !used && LocalDateTime.now().isBefore(expiresAt);
    }
    
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
    
    public long getHoursRemaining() {
        if (isExpired()) {
            return 0;
        }
        return java.time.temporal.ChronoUnit.HOURS.between(LocalDateTime.now(), expiresAt);
    }
    
    // Getters and Setters
    public String getReservationId() {
        return reservationId;
    }
    
    public void setReservationId(String reservationId) {
        this.reservationId = reservationId;
    }
    
    public Customer getCustomer() {
        return customer;
    }
    
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
    
    public ParkingSpace getReservedSpace() {
        return reservedSpace;
    }
    
    public void setReservedSpace(ParkingSpace reservedSpace) {
        this.reservedSpace = reservedSpace;
    }
    
    public VehicleType getVehicleType() {
        return vehicleType;
    }
    
    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
    
    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }
    
    public int getValidityHours() {
        return validityHours;
    }
    
    public void setValidityHours(int validityHours) {
        this.validityHours = validityHours;
    }
    
    public boolean isUsed() {
        return used;
    }
    
    public void setUsed(boolean used) {
        this.used = used;
    }
    
    @Override
    public String toString() {
        return "Reservation{" +
                "reservationId='" + reservationId + '\'' +
                ", customer=" + customer.getName() +
                ", space=" + reservedSpace.getSpaceId() +
                ", vehicleType=" + vehicleType +
                ", validUntil=" + expiresAt +
                ", used=" + used +
                '}';
    }
}