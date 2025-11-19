package com.phuc.payment.service;

import com.phuc.payment.dto.request.ChargeSessionRequest;
import com.phuc.event.dto.StripeChargeRequest;
import com.phuc.payment.dto.request.StripeSubscriptionRequest;
import com.phuc.payment.dto.request.SubscriptionSessionRequest;
import com.phuc.payment.dto.response.SessionResponse;
import com.phuc.payment.dto.response.StripeChargeResponse;
import com.phuc.payment.dto.response.StripeSubscriptionResponse;

import java.util.List;

public interface StripeService {

      StripeChargeResponse charge(StripeChargeRequest request);

      StripeChargeResponse processCharge(StripeChargeRequest request);

      StripeSubscriptionResponse createSubscription(StripeSubscriptionRequest request);

      SessionResponse createChargeSession(ChargeSessionRequest request);

      SessionResponse createSubscriptionSession(SubscriptionSessionRequest request);

      void cancelSubscription(String subscriptionId);

      List<StripeSubscriptionResponse> retrieveAllSubscriptions();

}