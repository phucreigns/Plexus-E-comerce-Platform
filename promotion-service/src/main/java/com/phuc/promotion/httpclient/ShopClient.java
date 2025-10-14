package com.phuc.promotion.httpclient;

import com.phuc.promotion.configuration.AuthenticationRequestInterceptor;
import com.phuc.promotion.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "shop-service", url = "${shop.service.url}", configuration = {AuthenticationRequestInterceptor.class})
public interface ShopClient {

    @GetMapping(value = "/{shopId}/exists", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Boolean> checkIfShopExists(@PathVariable("shopId") String shopId);

}