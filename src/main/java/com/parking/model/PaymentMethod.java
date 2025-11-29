package com.parking.model;

public enum PaymentMethod {
    CASH("Cash Payment"),
    CARD("Credit/Debit Card"),
    UPI("UPI Payment");
    
    private final String displayName;
    
    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
