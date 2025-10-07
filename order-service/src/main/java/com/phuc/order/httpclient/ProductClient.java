package com.phuc.order.httpclient;

import com.phuc.order.configuration.AuthenticationRequestInterceptor;
import com.phuc.order.dto.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@FeignClient(name = "product-service", url = "${product.service.url}", configuration = {AuthenticationRequestInterceptor.class})
public interface ProductClient {

    // Existing methods
    @PostMapping("/{productId}/variant/{variantId}/reduce-stock")
    ApiResponse<Void> reduceStock(@PathVariable String productId, 
                                @PathVariable String variantId, 
                                @RequestBody Map<String, Integer> request);
    
    @PostMapping("/{productId}/variant/{variantId}/restore-stock")
    ApiResponse<Void> restoreStock(@PathVariable String productId, 
                                  @PathVariable String variantId, 
                                  @RequestBody Map<String, Integer> request);

    // New methods requested
    @GetMapping("/{productId}/variant/{variantId}/price")
    ApiResponse<BigDecimal> getProductPriceById(@PathVariable String productId, 
                                             @PathVariable String variantId);
    
    @GetMapping("/{productId}/variant/{variantId}/stock")
    ApiResponse<Integer> getProductStockById(@PathVariable String productId, 
                                          @PathVariable String variantId);
    
    @PutMapping("/{productId}/variant/{variantId}/stock-sold")
    ApiResponse<Void> updateStockAndSoldQuantity(@PathVariable String productId,
                                               @PathVariable String variantId,
                                               @RequestBody Integer quantity);
}
