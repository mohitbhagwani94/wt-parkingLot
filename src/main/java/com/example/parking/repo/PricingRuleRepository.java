package com.example.parking.repo;

import com.example.parking.model.PricingRule;
import com.example.parking.model.VehicleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PricingRuleRepository extends JpaRepository<PricingRule, Long> {
    //Optional findByType(VehicleType type);
}