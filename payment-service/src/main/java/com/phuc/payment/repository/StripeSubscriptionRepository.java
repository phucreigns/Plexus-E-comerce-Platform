package com.phuc.payment.repository;

import com.phuc.payment.entity.StripeSubscription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StripeSubscriptionRepository extends JpaRepository<StripeSubscription, String> {}