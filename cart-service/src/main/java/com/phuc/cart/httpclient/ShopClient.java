package com.phuc.cart.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.phuc.cart.configuration.AuthenticationRequestInterceptor;
import com.phuc.cart.dto.ApiResponse;

@FeignClient(name = "shop-service", url = "${shop.service.url}", configuration = {AuthenticationRequestInterceptor.class})
public interface ShopClient {

      @GetMapping(value = "/{shopId}/owner", produces = MediaType.APPLICATION_JSON_VALUE)
      ApiResponse<String> getOwnerUsernameByShopId(@PathVariable("shopId") String shopId);

}
