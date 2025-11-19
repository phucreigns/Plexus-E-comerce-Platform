package com.phuc.payment.httpclient;

import com.phuc.payment.configuration.AuthenticationRequestInterceptor;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "order-service", url = "${order.service.url}", configuration = {AuthenticationRequestInterceptor.class})
public interface OrderClient {

    @PostMapping(value = "/webhook/payment/{orderId}/success", produces = MediaType.APPLICATION_JSON_VALUE)
    void markOrderPaid(@PathVariable("orderId") Long orderId);

}

