package com.phuc.cart.httpclient;

import com.phuc.cart.configuration.AuthenticationRequestInterceptor;
import com.phuc.cart.dto.ApiResponse;
import com.phuc.cart.httpclient.dto.CreateCheckoutSessionRequest;
import com.phuc.cart.httpclient.dto.SessionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "payment-service", url = "${payment.service.url}", configuration = {AuthenticationRequestInterceptor.class})
public interface PaymentClient {

    @PostMapping(value = "/stripe/session/charge", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<SessionResponse> createChargeSession(@RequestBody CreateCheckoutSessionRequest request);
}

