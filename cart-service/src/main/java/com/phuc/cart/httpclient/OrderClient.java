package com.phuc.cart.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.phuc.cart.configuration.AuthenticationRequestInterceptor;
import com.phuc.cart.httpclient.request.OrderCreationRequest;
import com.phuc.cart.httpclient.response.OrderResponse;

@FeignClient(name = "order-service", url = "${order.service.url}", configuration = {AuthenticationRequestInterceptor.class})
public interface OrderClient {

      @PostMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
      OrderResponse createOrder(@RequestBody OrderCreationRequest request);

}
