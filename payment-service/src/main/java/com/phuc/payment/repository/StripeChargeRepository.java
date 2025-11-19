package com.phuc.payment.repository;

import com.phuc.payment.entity.StripeCharge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StripeChargeRepository extends JpaRepository<StripeCharge, String> {}