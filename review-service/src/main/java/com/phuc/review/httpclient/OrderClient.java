package com.phuc.review.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import com.phuc.review.configuration.AuthenticationRequestInterceptor;
import com.phuc.review.dto.ApiResponse;
import com.phuc.review.httpclient.response.OrderResponse;
import java.util.List;

@FeignClient(name = "order-service", url = "${order.service.url}", configuration = {AuthenticationRequestInterceptor.class})
public interface OrderClient {

    @GetMapping(value = "/my-orders", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<OrderResponse>> getMyOrders();
    
}
