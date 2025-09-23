package com.example.parking.repo;

import com.example.parking.model.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Optional<Ticket> findByVehicle_PlateNumberAndActiveTrue(String plateNumber);
}
