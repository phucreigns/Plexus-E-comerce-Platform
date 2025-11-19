package com.phuc.order.httpclient;

import com.phuc.order.configuration.AuthenticationRequestInterceptor;
import com.phuc.order.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@FeignClient(name = "product-service", url = "${product.service.url}", configuration = {AuthenticationRequestInterceptor.class})
public interface ProductClient {

    @PostMapping(value = "/{productId}/variant/{variantId}/reduce-stock", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Void> reduceStock(@PathVariable String productId, 
                                @PathVariable String variantId, 
                                @RequestBody Map<String, Integer> request);
    
    @PostMapping(value = "/{productId}/variant/{variantId}/restore-stock", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Void> restoreStock(@PathVariable String productId, 
                                  @PathVariable String variantId, 
                                  @RequestBody Map<String, Integer> request);

    @GetMapping("/{productId}/variant/{variantId}/price")
    ApiResponse<BigDecimal> getProductPriceById(@PathVariable String productId, 
                                             @PathVariable String variantId);
    
    @GetMapping("/{productId}/variant/{variantId}/stock")
    ApiResponse<Integer> getProductStockById(@PathVariable String productId, 
                                          @PathVariable String variantId);
    
    @PutMapping(value = "/{productId}/variant/{variantId}/stock-sold", consumes = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Void> updateStockAndSoldQuantity(@PathVariable String productId,
                                               @PathVariable String variantId,
                                               @RequestBody Integer quantity);
}
