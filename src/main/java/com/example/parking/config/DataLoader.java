package com.example.parking.config;

import com.example.parking.model.*;
import com.example.parking.repo.ParkingSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.ArrayList;
import java.util.List;
import com.example.parking.model.PricingRule;
import com.example.parking.repo.PricingRuleRepository;

@Configuration
@RequiredArgsConstructor
public class DataLoader {
    @Bean
    CommandLineRunner init(ParkingSlotRepository slotRepo) {
        return args -> {
            if (slotRepo.count() > 0) return;
            List<ParkingSlot> slots = new ArrayList<>();
            for (int f = 1; f <= 3; f++) {
                for (int i = 1; i <= 10; i++) {
                    slots.add(ParkingSlot.builder()
                            .floor(f)
                            .slotNumber("F" + f + "-" + String.format("%02d", i))
                            .type(i % 3 == 0 ? VehicleType.TRUCK : (i % 2 == 0 ? VehicleType.CAR : VehicleType.BIKE))
                            .status(SlotStatus.FREE)
                            .build());
                }
            }
            slotRepo.saveAll(slots);
        };
    }
    @Bean
    CommandLineRunner seedPricing(com.example.parking.repo.PricingRuleRepository pricingRepo) {
        return args -> {
            if (pricingRepo.count() == 0) {
                pricingRepo.save(com.example.parking.model.PricingRule.builder()
                        .type(com.example.parking.model.VehicleType.BIKE).ratePerHour(10.0).build());
                pricingRepo.save(com.example.parking.model.PricingRule.builder()
                        .type(com.example.parking.model.VehicleType.CAR).ratePerHour(40.0).build());
                pricingRepo.save(com.example.parking.model.PricingRule.builder()
                        .type(com.example.parking.model.VehicleType.TRUCK).ratePerHour(100.0).build());
            }
        };
    }
}
