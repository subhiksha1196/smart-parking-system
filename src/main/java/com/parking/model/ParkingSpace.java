package com.parking.model;

import jakarta.persistence.*;

@Entity
@Table(name = "parking_spaces")
public class ParkingSpace {
    @Id
    private String spaceId;
    
    private int floorId;
    private String zoneId;
    
    @Enumerated(EnumType.STRING)
    private VehicleType compatibleVehicleType;
    
    @Enumerated(EnumType.STRING)
    private SpaceStatus status;
    
    private boolean handicappedSpace;
    
    @ManyToOne
    @JoinColumn(name = "current_vehicle_plate")
    private Vehicle currentVehicle;
    
    public ParkingSpace() {}
    
    public ParkingSpace(String spaceId, int floorId, String zoneId, VehicleType compatibleVehicleType) {
        this.spaceId = spaceId;
        this.floorId = floorId;
        this.zoneId = zoneId;
        this.compatibleVehicleType = compatibleVehicleType;
        this.status = SpaceStatus.AVAILABLE;
        this.handicappedSpace = false;
    }
    
    public ParkingSpace(String spaceId, int floorId, String zoneId, VehicleType compatibleVehicleType, boolean handicappedSpace) {
        this(spaceId, floorId, zoneId, compatibleVehicleType);
        this.handicappedSpace = handicappedSpace;
    }
    
    public boolean isAvailable() {
        return status == SpaceStatus.AVAILABLE;
    }
    
    public void occupySpace(Vehicle vehicle) {
        this.currentVehicle = vehicle;
        this.status = SpaceStatus.OCCUPIED;
    }
    
    public void releaseSpace() {
        this.currentVehicle = null;
        this.status = SpaceStatus.AVAILABLE;
    }
    
    public void reserve() {
        this.status = SpaceStatus.RESERVED;
    }
    
    // Getters and Setters
    public String getSpaceId() { return spaceId; }
    public void setSpaceId(String spaceId) { this.spaceId = spaceId; }
    
    public int getFloorId() { return floorId; }
    public void setFloorId(int floorId) { this.floorId = floorId; }
    
    public String getZoneId() { return zoneId; }
    public void setZoneId(String zoneId) { this.zoneId = zoneId; }
    
    public SpaceStatus getStatus() { return status; }
    public void setStatus(SpaceStatus status) { this.status = status; }
    
    public VehicleType getCompatibleVehicleType() { return compatibleVehicleType; }
    public void setCompatibleVehicleType(VehicleType type) { this.compatibleVehicleType = type; }
    
    public Vehicle getCurrentVehicle() { return currentVehicle; }
    public void setCurrentVehicle(Vehicle vehicle) { this.currentVehicle = vehicle; }
    
    public boolean isHandicappedSpace() { return handicappedSpace; }
    public void setHandicappedSpace(boolean handicappedSpace) { this.handicappedSpace = handicappedSpace; }
}
