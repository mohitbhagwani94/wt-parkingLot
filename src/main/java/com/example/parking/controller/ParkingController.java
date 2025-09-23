package com.example.parking.controller;

import com.example.parking.model.Payment;
import com.example.parking.model.Ticket;
import com.example.parking.model.VehicleType;
import com.example.parking.service.ParkingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/parking")
@RequiredArgsConstructor
public class ParkingController {
    private final ParkingService parkingService;

    record ParkRequest(String plateNumber, VehicleType type, String ownerName) {}

    @PostMapping("/park")
    public ResponseEntity<Ticket> park(@RequestBody ParkRequest req) {
        Ticket t = parkingService.parkVehicle(req.plateNumber(), req.type(), req.ownerName());
        return ResponseEntity.created(URI.create("/api/parking/ticket/" + t.getId())).body(t);
    }

//    @PostMapping("/exit/{plate}")
//    public ResponseEntity<Payment> exit(@PathVariable("plate") String plate) {
//        Payment p = parkingService.exitVehicle(plate);
//        return ResponseEntity.ok(p);
//    }

    @PostMapping("/exit/{plate}")
    public ResponseEntity<com.example.parking.model.dto.PaymentReceipt> exit(
            @PathVariable("plate") String plate,
            @RequestBody(required = false) PayRequest req) {
        boolean approve = req == null || req.approve() == null ? true : req.approve();
        var receipt = parkingService.exitVehicle(plate, approve);
        return ResponseEntity.ok(receipt);

    }

    record PayRequest(Boolean approve) {}

    @GetMapping("/quote/{plate}")
    public ResponseEntity<com.example.parking.model.dto.PaymentReceipt> quote(@PathVariable("plate") String plate) {
        var receipt = parkingService.quoteExit(plate);
        return ResponseEntity.ok(receipt);
    
    @PostMapping("/exit/{plate}")
    public ResponseEntity<Receipt> exit(@PathVariable String plate) {
        Receipt receipt = parkingService.exit(plate);
        return ResponseEntity.ok(receipt);
    }

    @PostMapping("/pay/{ticketId}")
    public ResponseEntity<Payment> pay(@PathVariable Long ticketId) {
        Payment payment = parkingService.pay(ticketId);
        return ResponseEntity.ok(payment);
    }

}
