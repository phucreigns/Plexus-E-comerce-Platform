package com.phuc.gateway.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;

@Configuration
public class JwtDecoderConfig {

      @Value("${auth0.jwks-uri:https://dev-xxxxx.us.auth0.com/.well-known/jwks.json}")
      private String auth0JwksUri;

      @Bean
      public ReactiveJwtDecoder jwtDecoder() {
            return NimbusReactiveJwtDecoder.withJwkSetUri(auth0JwksUri).build();
      }

}