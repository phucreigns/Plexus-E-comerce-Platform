package com.phuc.cart.httpclient;

import com.phuc.cart.configuration.AuthenticationRequestInterceptor;
import com.phuc.cart.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.phuc.cart.httpclient.response.ExistsResponse;

@FeignClient(name = "product-service", url = "${product.service.url}", configuration = AuthenticationRequestInterceptor.class)
public interface ProductClient {

    @GetMapping(value = "/{productId}/price/{variantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Double> getProductPriceById(@PathVariable("productId") String productId,
                                            @PathVariable("variantId") String variantId);
    @GetMapping(value = "/{productId}/stock/{variantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Integer> getProductStockById(@PathVariable("productId") String productId,
                                             @PathVariable("variantId") String variantId);
    @GetMapping("/{productId}/exists/{variantId}")
    ApiResponse<ExistsResponse> existsProduct(@PathVariable String productId, @PathVariable String variantId);

    @GetMapping(value = "/{productId}/shopId", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<String> getShopIdByProductId(@PathVariable("productId") String productId);

}
