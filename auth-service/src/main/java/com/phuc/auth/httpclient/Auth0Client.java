package com.phuc.auth.httpclient;

import com.phuc.auth.dto.auth0.Auth0TokenResponse;
import com.phuc.auth.dto.auth0.Auth0UserInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "auth0-client", url = "https://${auth0.domain}")
public interface Auth0Client {

    @PostMapping(value = "/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Auth0TokenResponse exchangeCodeForToken(@RequestBody String formData);

    @PostMapping(value = "/oauth/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    Auth0TokenResponse refreshToken(@RequestBody String formData);

    @GetMapping("/userinfo")
    Auth0UserInfo getUserInfo(@RequestHeader("Authorization") String authorization);

}