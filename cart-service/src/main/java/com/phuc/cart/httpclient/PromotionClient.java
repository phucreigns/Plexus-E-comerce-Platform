package com.phuc.cart.httpclient;

import com.phuc.cart.configuration.AuthenticationRequestInterceptor;
import com.phuc.cart.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "promotion-service", url = "${promotion.service.url}", configuration = {AuthenticationRequestInterceptor.class})
public interface PromotionClient {

    @PostMapping(value = "/apply", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Void> applyPromotionCode(@RequestParam String promoCode, @RequestParam Long cartId);

}