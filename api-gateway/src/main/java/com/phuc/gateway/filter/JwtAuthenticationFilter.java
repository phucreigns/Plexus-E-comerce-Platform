package com.phuc.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		ServerHttpRequest request = exchange.getRequest();

		// Forward Authorization header to downstream services
		String authHeader = request.getHeaders().getFirst("Authorization");
		if (authHeader != null) {
			log.debug("Forwarding Authorization header to downstream service");
		}

		return chain.filter(exchange);
	}

	@Override
	public int getOrder() {
		return -100;
	}
}



