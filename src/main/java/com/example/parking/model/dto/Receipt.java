package com.example.parking.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class Receipt {
    private Long ticketId;
    private String plateNumber;
    private String vehicleType;
    private LocalDateTime entryTime;
    private LocalDateTime exitTime;
    private long durationMinutes;
    private double amount;
    private boolean paid;
}
