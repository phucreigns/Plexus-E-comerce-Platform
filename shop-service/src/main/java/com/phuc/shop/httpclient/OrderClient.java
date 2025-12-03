package com.phuc.shop.httpclient;

import com.phuc.shop.configuration.AuthenticationRequestInterceptor;
import com.phuc.shop.httpclient.response.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "order-service", url = "${order.service.url}", configuration = {AuthenticationRequestInterceptor.class})
public interface OrderClient {

    @GetMapping(value = "/orders", produces = MediaType.APPLICATION_JSON_VALUE)
    List<OrderResponse> getOrdersByDateRange(
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate
    );
}

