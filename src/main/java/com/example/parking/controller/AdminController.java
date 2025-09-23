package com.example.parking.controller;

import com.example.parking.model.ParkingSlot;
import com.example.parking.model.PricingRule;
import com.example.parking.model.SlotStatus;
import com.example.parking.model.VehicleType;
import com.example.parking.repo.ParkingSlotRepository;
import com.example.parking.repo.PricingRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final ParkingSlotRepository slotRepo;
    private final PricingRuleRepository pricingRepo;
    @PostMapping("/slot")
    public ResponseEntity<ParkingSlot> addSlot(@RequestBody AddSlotRequest req) {
        ParkingSlot slot = ParkingSlot.builder()
                .floor(req.floor())
                .slotNumber(req.slotNumber())
                .type(req.type())
                .status(SlotStatus.FREE)
                .build();
        return ResponseEntity.ok(slotRepo.save(slot));
    }

    @DeleteMapping("/slot/{id}")
    public ResponseEntity<String> removeSlot(@PathVariable Long id) {
        if (slotRepo.existsById(id)) {
            slotRepo.deleteById(id);
            return ResponseEntity.ok("Deleted slot " + id);
        }
        return ResponseEntity.badRequest().body("Slot not found");
    }

    @PostMapping("/pricing")
    public ResponseEntity<PricingRule> createPricingRule(@RequestBody PricingRule rule) {
        PricingRule saved = pricingRepo.save(rule);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/pricing/{type}")
    public ResponseEntity<PricingRule> getPricingRule(@PathVariable VehicleType type) {
        PricingRule rule = pricingRepo.findAll().stream()
                .filter(r -> r.getType().equals(type))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("PricingRule not found for type: " + type));
        return ResponseEntity.ok(rule);
    }

    @PutMapping("/pricing/{id}")
    public ResponseEntity<PricingRule> updatePricingRule(
            @PathVariable Long id,
            @RequestBody PricingRule updatedRule) {
        PricingRule rule = pricingRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("PricingRule not found"));

        rule.setRatePerHour(updatedRule.getRatePerHour());
        rule.setType(updatedRule.getType());

        PricingRule saved = pricingRepo.save(rule);
        return ResponseEntity.ok(saved);
    }

    public record AddSlotRequest(int floor, String slotNumber, VehicleType type) {}

}