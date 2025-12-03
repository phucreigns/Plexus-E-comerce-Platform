package com.phuc.gateway.configuration;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationFilter implements GlobalFilter, Ordered {

      ReactiveJwtDecoder jwtDecoder;

      String[] publicEndpoints = {
              "/actuator/**",
              "/login",
              "/refresh",
              "/logout",
              "/reset-password",
              "/swagger-ui.html",
              "/swagger-ui/**",
              "/auth/swagger-ui/**",
              "/product/swagger-ui/**",
              "/shop/swagger-ui/**",
              "/cart/swagger-ui/**",
              "/order/swagger-ui/**",
              "/file/swagger-ui/**",
              "/payment/swagger-ui/**",
              "/promotion/swagger-ui/**",
              "/review/swagger-ui/**",
              "/notification/swagger-ui/**",
              "/v3/api-docs/**",
              "/auth/v3/api-docs/**",
              "/product/v3/api-docs/**",
              "/shop/v3/api-docs/**",
              "/cart/v3/api-docs/**",
              "/order/v3/api-docs/**",
              "/file/v3/api-docs/**",
              "/payment/v3/api-docs/**",
              "/promotion/v3/api-docs/**",
              "/review/v3/api-docs/**",
              "/notification/v3/api-docs/**",
              "/swagger-resources/**",
              "/auth/swagger-resources/**",
              "/product/swagger-resources/**",
              "/shop/swagger-resources/**",
              "/cart/swagger-resources/**",
              "/order/swagger-resources/**",
              "/file/swagger-resources/**",
              "/payment/swagger-resources/**",
              "/promotion/swagger-resources/**",
              "/review/swagger-resources/**",
              "/notification/swagger-resources/**",
              "/api-docs/**",
              "/aggregate/**",
              "/payment/stripe/webhook"
      };

      @Value("${app.api-prefix:/api/v1}")
      @NonFinal
      String apiPrefix;

      @Override
      public int getOrder() {
            return -1;
      }

      @Override
      public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
            ServerHttpResponse response = exchange.getResponse();
            ServerHttpRequest request = exchange.getRequest();

            response.getHeaders().add(
                    "Strict-Transport-Security",
                    "max-age=31536000; includeSubDomains");

            if (isPublicEndpoint(request)) {
                  return chain.filter(exchange);
            }

            List<String> authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || CollectionUtils.isEmpty(authHeader)) {
                  return unauthenticated(response);
            }

            String authValue = authHeader.getFirst();
            if (authValue == null || !authValue.startsWith("Bearer ")) {
                  return unauthenticated(response);
            }

            String token = authValue.replace("Bearer ", "");

            return jwtDecoder.decode(token)
                    .flatMap(jwt -> chain.filter(exchange))
                    .onErrorResume(throwable -> {
                          log.error("Authentication error: {}", throwable.getMessage());
                          if (throwable instanceof JwtException) {
                                return unauthenticated(response);
                          } else {
                                return serviceUnavailable(response);
                          }
                    });
      }

      private boolean isPublicEndpoint(ServerHttpRequest request) {
            String path = request.getURI().getPath();
            return Arrays.stream(publicEndpoints).anyMatch(endpoint -> {
                  // Xử lý pattern với ** (wildcard)
                  if (endpoint.endsWith("/**")) {
                        String basePath = endpoint.substring(0, endpoint.length() - 3);
                        return path.startsWith(apiPrefix + basePath) || 
                               path.startsWith(basePath);
                  }
                  
                  // Exact match hoặc match với trailing slash
                  return path.equals(apiPrefix + endpoint) || 
                         path.startsWith(apiPrefix + endpoint + "/") ||
                         path.equals(endpoint) ||
                         path.startsWith(endpoint + "/");
            });
      }

      private Mono<Void> unauthenticated(ServerHttpResponse response) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            byte[] errorMessage = "{\"error\":\"Unauthorized\"}".getBytes();
            return response.writeWith(Mono.just(response.bufferFactory().wrap(errorMessage)));
      }

      private Mono<Void> serviceUnavailable(ServerHttpResponse response) {
            response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            byte[] errorMessage = "{\"error\":\"Service Unavailable\"}".getBytes();
            return response.writeWith(Mono.just(response.bufferFactory().wrap(errorMessage)));
      }

}