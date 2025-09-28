package com.phuc.shop.httpclient;

import com.phuc.shop.configuration.AuthenticationRequestInterceptor;
import com.phuc.shop.dto.ApiResponse;
import com.phuc.shop.httpclient.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "product-service", url = "${product.service.url}", configuration = {AuthenticationRequestInterceptor.class})
public interface ProductClient {

    @GetMapping(value ="/product/{productId}",produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<ProductResponse>> getProductsByShopId(@PathVariable("productId") String productId);

}