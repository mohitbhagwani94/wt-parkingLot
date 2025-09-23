package com.example.parking.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"type"}))
public class PricingRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private VehicleType type;

    @Column(nullable = false)
    private double ratePerHour;

}