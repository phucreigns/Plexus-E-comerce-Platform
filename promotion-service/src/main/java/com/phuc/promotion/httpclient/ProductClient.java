package com.phuc.promotion.httpclient;

import com.phuc.promotion.configuration.AuthenticationRequestInterceptor;
import com.phuc.promotion.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "product-service", url = "${product.service.url}", configuration = AuthenticationRequestInterceptor.class)
public interface ProductClient {

    @GetMapping(value = "/{productId}/exists", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Boolean> isProductExist(@PathVariable String productId);

    @GetMapping(value = "/{productId}/shopId", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<String> getShopIdByProductId(@PathVariable("productId") String productId);

}

