package com.parking.repository;

import com.parking.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, String> {
    List<Reservation> findByUsedFalse();
    List<Reservation> findByCustomerId(Long customerId);
}
