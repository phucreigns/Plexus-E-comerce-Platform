package com.phuc.payment.mapper;

import com.phuc.event.dto.StripeChargeRequest;
import com.phuc.payment.dto.request.*;
import com.phuc.payment.dto.response.*;
import com.phuc.payment.entity.*;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StripeMapper {

      StripeCharge toStripeCharge(StripeChargeRequest request);

      StripeChargeResponse toStripeChargeResponse(StripeCharge stripeCharge);

      StripeSubscription toStripeSubscription(StripeSubscriptionRequest request);

      StripeSubscriptionResponse toStripeSubscriptionResponse(StripeSubscription stripeSubscription);

      Session toSession(ChargeSessionRequest request);

      SessionResponse toSessionResponse(Session session);

}