package com.example.parking.service;

import com.example.parking.model.*;
import com.example.parking.model.dto.PaymentReceipt;
import com.example.parking.model.dto.Receipt;
import com.example.parking.repo.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ParkingService {
    private final ParkingSlotRepository slotRepo;
    private final VehicleRepository vehicleRepo;
    private final TicketRepository ticketRepo;
    private final PaymentRepository paymentRepo;
    private final PricingRuleRepository pricingRepo; // only once

    private double ratePerHour(VehicleType type) {
        return pricingRepo.findByType(type)
                .map((double) r -> (double) r.getRatePerHour()) // lambda avoids Object ambiguity
                .orElseGet(() -> switch (type) {
                    case BIKE -> 10.0;
                    case CAR -> 40.0;
                    case TRUCK -> 100.0;
                });
    }

    public double ratePerHour(Long ruleId, Instant entry, Instant exit) {
        PricingRule rule = pricingRepo.findById(ruleId)
                .orElseThrow(() -> new RuntimeException("Rule not found"));

        long hours = Math.max(1, Duration.between(entry, exit).toHours());
        return hours * rule.getRatePerHour();
    }

    @Transactional
    public Ticket parkVehicle(String plateNumber, VehicleType type, String ownerName) {
        var existing = ticketRepo.findByVehicle_PlateNumberAndActiveTrue(plateNumber);
        if (existing.isPresent()) throw new IllegalStateException("Vehicle already parked");

        var vehicle = vehicleRepo.findByPlateNumber(plateNumber)
                .orElseGet(() -> vehicleRepo.save(Vehicle.builder()
                        .plateNumber(plateNumber)
                        .type(type)
                        .ownerName(ownerName)
                        .build()));

        List<ParkingSlot> freeSlots = slotRepo.findAndLockFreeSlots(type);
        if (freeSlots.isEmpty()) throw new IllegalStateException("Parking full for type: " + type);

        ParkingSlot chosen = freeSlots.get(0);
        chosen.setStatus(SlotStatus.OCCUPIED);
        slotRepo.save(chosen);

        Ticket ticket = Ticket.builder()
                .vehicle(vehicle)
                .slot(chosen)
                .entryTime(Instant.now())
                .paid(false)
                .active(true)
                .build();
        return ticketRepo.save(ticket);
    }

    // New: quote charges without changing state
    public PaymentReceipt quoteExit(String plateNumber) {
        Ticket ticket = ticketRepo.findByVehicle_PlateNumberAndActiveTrue(plateNumber)
                .orElseThrow(() -> new IllegalStateException("Active ticket not found"));

        Instant now = Instant.now();
        long minutes = Duration.between(ticket.getEntryTime(), now).toMinutes();
        double hours = Math.max(0, Math.ceil(minutes / 60.0));
        double billableHours = Math.max(0, hours - 2); // first 2 hours free
        double amount = billableHours * ratePerHour(ticket.getVehicle().getType());

        return new PaymentReceipt(
                null,
                ticket.getId(),
                ticket.getVehicle().getPlateNumber(),
                ticket.getSlot().getSlotNumber(),
                ticket.getSlot().getFloor(),
                ticket.getEntryTime(),
                now,
                minutes,
                amount,
                "INR",
                PaymentStatus.PENDING
        );
    }

    // Updated: perform payment and free slot only on success
    @Transactional
    public PaymentReceipt exitVehicle(String plateNumber, boolean approve) {
        Ticket ticket = ticketRepo.findByVehicle_PlateNumberAndActiveTrue(plateNumber)
                .orElseThrow(() -> new IllegalStateException("Active ticket not found"));

        // Idempotency: if already paid/closed, return last payment receipt
        if (ticket.isPaid() || !ticket.isActive()) {
            // find latest payment for the ticket (assumes one-to-one in model)
            Payment existing = paymentRepo.findAll().stream()
                    .filter(p -> p.getTicket().getId().equals(ticket.getId()))
                    .reduce((first, second) -> second) // last
                    .orElse(null);
            Instant exitTime = ticket.getExitTime() != null ? ticket.getExitTime() : Instant.now();
            long minutes = Duration.between(ticket.getEntryTime(), exitTime).toMinutes();
            double amount = existing != null ? existing.getAmount() : 0.0;
            PaymentStatus status = existing != null ? existing.getStatus() : PaymentStatus.SUCCESS;

            return new PaymentReceipt(
                    existing != null ? existing.getId() : null,
                    ticket.getId(),
                    ticket.getVehicle().getPlateNumber(),
                    ticket.getSlot().getSlotNumber(),
                    ticket.getSlot().getFloor(),
                    ticket.getEntryTime(),
                    exitTime,
                    minutes,
                    amount,
                    "INR",
                    status
            );
        }

        Instant exit = Instant.now();
        ticket.setExitTime(exit);

        long minutes = Duration.between(ticket.getEntryTime(), exit).toMinutes();
        double hours = Math.max(0, Math.ceil(minutes / 60.0));
        double billableHours = Math.max(0, hours - 2);
        double amount = billableHours * ratePerHour(ticket.getVehicle().getType());

        PaymentStatus status = approve ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;

        Payment payment = Payment.builder()
                .ticket(ticket)
                .amount(amount)
                .status(status)
                .timestamp(Instant.now())
                .build();
        payment = paymentRepo.save(payment);

        if (status == PaymentStatus.SUCCESS) {
            // free slot + close ticket atomically
            ParkingSlot slot = ticket.getSlot();
            slot.setStatus(SlotStatus.FREE);
            slotRepo.save(slot);

            ticket.setPaid(true);
            ticket.setActive(false);
            ticketRepo.save(ticket);
        } else {
            // Payment failed: do NOT free slot, keep ticket active/unpaid
            ticketRepo.save(ticket);
        }

        return new PaymentReceipt(
                payment.getId(),
                ticket.getId(),
                ticket.getVehicle().getPlateNumber(),
                ticket.getSlot().getSlotNumber(),
                ticket.getSlot().getFloor(),
                ticket.getEntryTime(),
                ticket.getExitTime() != null ? ticket.getExitTime() : exit,
                minutes,
                amount,
                "INR",
                status
        );
    
    @Transactional
    public Receipt exit(String plateNumber) {
        Ticket ticket = ticketRepository.findByPlateNumberAndActiveTrue(plateNumber)
                .orElseThrow(() -> new RuntimeException("Active ticket not found for plate: " + plateNumber));

        ticket.setExitTime(LocalDateTime.now());

        long minutes = Duration.between(ticket.getEntryTime(), ticket.getExitTime()).toMinutes();
        if (minutes == 0) minutes = 1; // minimum 1 minute

        PricingRule rule = pricingRuleRepository.findByType(ticket.getVehicle().getType())
                .orElseThrow(() -> new RuntimeException("No pricing rule found"));

        double amount = Math.ceil(minutes / 60.0) * rule.getRatePerHour();
        ticket.setAmount(amount);

        ticketRepository.save(ticket);

        return new Receipt(
                ticket.getId(),
                ticket.getVehicle().getPlateNumber(),
                ticket.getVehicle().getType().name(),
                ticket.getEntryTime(),
                ticket.getExitTime(),
                minutes,
                amount,
                ticket.isPaid()
        );
    }

    @Transactional
    public Payment pay(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (ticket.isPaid()) {
            throw new RuntimeException("Ticket already paid");
        }

        Payment payment = new Payment();
        payment.setTicket(ticket);
        payment.setAmount(ticket.getAmount());
        payment.setTime(LocalDateTime.now());
        payment.setStatus("SUCCESS");
        paymentRepository.save(payment);

        ticket.setPaid(true);
        ticket.setActive(false);
        ticketRepository.save(ticket);

        ParkingSlot slot = ticket.getSlot();
        slot.setStatus(SlotStatus.FREE);
        slotRepository.save(slot);

        return payment;
    }

}
