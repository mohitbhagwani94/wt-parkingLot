package com.example.parking.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String plateNumber;

    @Enumerated(EnumType.STRING)
    private VehicleType type;

    private String ownerName;
}
