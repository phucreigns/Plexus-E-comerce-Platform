package com.phuc.cart.httpclient;

import com.phuc.cart.configuration.AuthenticationRequestInterceptor;
import com.phuc.cart.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", url = "${PRODUCT_SERVICE_URL:http://localhost:8091}", configuration = {AuthenticationRequestInterceptor.class})
public interface ProductClient {

    @GetMapping("/product/{productId}")
    ApiResponse<Object> getProductById(@PathVariable String productId);
    
    @GetMapping("/product/{productId}/exists")
    ApiResponse<Boolean> checkProductExists(@PathVariable String productId);
}
