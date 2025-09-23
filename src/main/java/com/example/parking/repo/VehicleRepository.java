package com.example.parking.repo;

import com.example.parking.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    Optional<Vehicle> findByPlateNumber(String plateNumber);
}
