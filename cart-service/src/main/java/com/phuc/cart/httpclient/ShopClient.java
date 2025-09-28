package com.phuc.cart.httpclient;


import com.phuc.cart.configuration.AuthenticationRequestInterceptor;
import com.phuc.cart.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "file-service", url = "${file.service.url}", configuration = {AuthenticationRequestInterceptor.class})
public interface ShopClient {
    @GetMapping(value = "/{shopId}/owner", consumes = "multipart/form-data", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<String> getOwnerEmailByShopId(@PathVariable("shopId") String shopId);

}
