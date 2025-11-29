package com.parking.repository;

import com.parking.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ParkingSpaceRepository extends JpaRepository<ParkingSpace, String> {
    List<ParkingSpace> findByCompatibleVehicleType(VehicleType type);
    List<ParkingSpace> findByFloorId(int floorId);
    List<ParkingSpace> findByStatusAndCompatibleVehicleType(SpaceStatus status, VehicleType type);
    List<ParkingSpace> findByStatusAndCompatibleVehicleTypeAndHandicappedSpace(
        SpaceStatus status, VehicleType type, boolean handicappedSpace
    );
    Long countByCompatibleVehicleType(VehicleType type);
    Long countByStatusAndCompatibleVehicleType(SpaceStatus status, VehicleType type);
    
}
