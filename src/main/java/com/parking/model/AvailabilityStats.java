package com.parking.model;

public class AvailabilityStats {
    private Integer total;
    private Integer available;
    private Integer occupied;

    public AvailabilityStats() {
        this.total = 0;
        this.available = 0;
        this.occupied = 0;
    }

    public AvailabilityStats(Integer total, Integer available, Integer occupied) {
        this.total = total != null ? total : 0;
        this.available = available != null ? available : 0;
        this.occupied = occupied != null ? occupied : 0;
    }

    public Integer getTotal() { return total; }
    public void setTotal(Integer total) { this.total = total; }
    
    public Integer getAvailable() { return available; }
    public void setAvailable(Integer available) { this.available = available; }
    
    public Integer getOccupied() { return occupied; }
    public void setOccupied(Integer occupied) { this.occupied = occupied; }
}