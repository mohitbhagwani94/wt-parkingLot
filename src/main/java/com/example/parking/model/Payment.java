package com.example.parking.model;

import com.example.parking.model.PaymentStatus;
import com.example.parking.model.Ticket;
import jakarta.persistence.*;
import lombok.*;


import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(indexes = {
        @Index(name = "idx_payment_ticket", columnList = "ticket_id")
})
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(optional = false)
    private Ticket ticket;

    private double amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;

    private Instant timestamp;

} 