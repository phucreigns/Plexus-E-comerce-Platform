package com.phuc.review.httpclient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.phuc.review.configuration.AuthenticationRequestInterceptor;
import com.phuc.review.dto.ApiResponse;
import com.phuc.review.httpclient.response.ExistsResponse;
import com.phuc.review.httpclient.response.ProductResponse;

@FeignClient(name = "product-service", url = "${product.service.url}", configuration = {AuthenticationRequestInterceptor.class})
public interface ProductClient {

    @GetMapping(value = "/{productId}/exists/{variantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<ExistsResponse> existsProduct(@PathVariable("productId") String productId,
                                              @PathVariable("variantId") String variantId);

    @GetMapping(value = "/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<ProductResponse> getProductById(@PathVariable String productId);
}
