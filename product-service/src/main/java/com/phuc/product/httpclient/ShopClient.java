package com.phuc.product.httpclient;

import com.phuc.product.configuration.AuthenticationRequestInterceptor;
import com.phuc.product.dto.ApiResponse;
import com.phuc.product.httpclient.response.ShopResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "shop-service", url = "${shop.service.url}", configuration = {AuthenticationRequestInterceptor.class})
public interface ShopClient {

    @GetMapping(value = "/owner/{email}", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<ShopResponse> getShopByOwnerEmail(@PathVariable(value = "email") String email);

}

