package com.phuc.promotion.httpclient;

import com.phuc.promotion.configuration.AuthenticationRequestInterceptor;
import com.phuc.promotion.httpclient.response.CartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "cart-service", url = "${cart.service.url}", configuration = AuthenticationRequestInterceptor.class)
public interface CartClient {

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    CartResponse getMyCart();

    @PutMapping(value = "/update-total", produces = MediaType.APPLICATION_JSON_VALUE)
    void updateCartTotal(@RequestParam("email") String email, @RequestBody double total);

    @GetMapping(value = "/by-email", produces = MediaType.APPLICATION_JSON_VALUE)
    CartResponse getCartByEmail(@RequestParam("email") String email);

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    CartResponse getCartById(@PathVariable("id") Long id);

    @PutMapping(value = "/{id}/update-total", produces = MediaType.APPLICATION_JSON_VALUE)
    void updateCartTotalById(@PathVariable("id") Long id, @RequestBody double total);

}
