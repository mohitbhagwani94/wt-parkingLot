package com.example.parking.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(indexes = {@Index(columnList = "floor,slotNumber")})
public class ParkingSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int floor;
    private String slotNumber;

    @Enumerated(EnumType.STRING)
    private VehicleType type;

    @Enumerated(EnumType.STRING)
    private SlotStatus status;
}
