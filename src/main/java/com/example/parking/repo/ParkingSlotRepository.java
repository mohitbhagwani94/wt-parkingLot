package com.example.parking.repo;

import com.example.parking.model.ParkingSlot;
import com.example.parking.model.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.List;

public interface ParkingSlotRepository extends JpaRepository<ParkingSlot, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from ParkingSlot s where s.type = :type and s.status = 'FREE' order by s.floor, s.slotNumber")
    List<ParkingSlot> findAndLockFreeSlots(@Param("type") VehicleType type);
}
