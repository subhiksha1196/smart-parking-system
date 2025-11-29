package com.parking.repository;

import com.parking.model.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ParkingTicketRepository extends JpaRepository<ParkingTicket, String> {
    List<ParkingTicket> findByStatus(TicketStatus status);
}


