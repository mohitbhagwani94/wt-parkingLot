package com.example.parking.model.dto;

import com.example.parking.model.PaymentStatus;
import java.time.Instant;

public record PaymentReceipt(
        Long paymentId,
        Long ticketId,
        String plateNumber,
        String slotNumber,
        int floor,
        Instant entryTime,
        Instant exitTime,
        long durationMinutes,
        double amount,
        String currency,
        PaymentStatus status
) {}