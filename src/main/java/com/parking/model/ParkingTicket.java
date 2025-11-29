package com.parking.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
public class ParkingTicket {
    @Id
    private String ticketId;
    
    @ManyToOne
    @JoinColumn(name = "vehicle_plate")
    private Vehicle vehicle;
    
    @ManyToOne
    @JoinColumn(name = "space_id")
    private ParkingSpace allocatedSpace;
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private double amount;
    
    @Enumerated(EnumType.STRING)
    private TicketStatus status;
    
    // Payment fields
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    
    private String paymentDetails;
    private double cashReceived;
    private double changeReturned;
    private boolean loyaltyDiscountApplied;
    private int loyaltyPointsEarned;
    
    public ParkingTicket() {}
    
    public ParkingTicket(String ticketId, Vehicle vehicle, ParkingSpace space, Customer customer) {
        this.ticketId = ticketId;
        this.vehicle = vehicle;
        this.allocatedSpace = space;
        this.customer = customer;
        this.entryTime = LocalDateTime.now();
        this.status = TicketStatus.ACTIVE;
        this.amount = 0.0;
        this.loyaltyDiscountApplied = false;
        this.loyaltyPointsEarned = 0;
    }
    
    // Getters and Setters
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }
    
    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }
    
    public ParkingSpace getAllocatedSpace() { return allocatedSpace; }
    public void setAllocatedSpace(ParkingSpace allocatedSpace) { this.allocatedSpace = allocatedSpace; }
    
    public Customer getCustomer() { return customer; }
    public void setCustomer(Customer customer) { this.customer = customer; }
    
    public LocalDateTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalDateTime entryTime) { this.entryTime = entryTime; }
    
    public LocalDateTime getExitTime() { return exitTime; }
    public void setExitTime(LocalDateTime exitTime) { this.exitTime = exitTime; }
    
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    
    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }
    
    public PaymentMethod getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(PaymentMethod paymentMethod) { this.paymentMethod = paymentMethod; }
    
    public String getPaymentDetails() { return paymentDetails; }
    public void setPaymentDetails(String paymentDetails) { this.paymentDetails = paymentDetails; }
    
    public double getCashReceived() { return cashReceived; }
    public void setCashReceived(double cashReceived) { this.cashReceived = cashReceived; }
    
    public double getChangeReturned() { return changeReturned; }
    public void setChangeReturned(double changeReturned) { this.changeReturned = changeReturned; }
    
    public boolean isLoyaltyDiscountApplied() { return loyaltyDiscountApplied; }
    public void setLoyaltyDiscountApplied(boolean loyaltyDiscountApplied) { 
        this.loyaltyDiscountApplied = loyaltyDiscountApplied; 
    }
    
    public int getLoyaltyPointsEarned() { return loyaltyPointsEarned; }
    public void setLoyaltyPointsEarned(int loyaltyPointsEarned) { 
        this.loyaltyPointsEarned = loyaltyPointsEarned; 
    }
}
