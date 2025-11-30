package com.phuc.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoders;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

	@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:}")
	private String issuerUri;

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		http.csrf(ServerHttpSecurity.CsrfSpec::disable);

		// Check if AUTH0_DOMAIN is configured
		boolean hasAuth0 = issuerUri != null && !issuerUri.isEmpty() && !issuerUri.equals("https://");

		if (hasAuth0) {
			// Enable OAuth2 security
			ReactiveJwtDecoder decoder = ReactiveJwtDecoders.fromOidcIssuerLocation(issuerUri);
			http
				.authorizeExchange(exchanges -> exchanges
					.pathMatchers("/api/auth/**").permitAll()
					.pathMatchers("/actuator/**").permitAll()
					.pathMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
					.anyExchange().authenticated()
				)
				.oauth2ResourceServer(oauth2 -> oauth2
					.jwt(jwt -> jwt.jwtDecoder(decoder))
				);
		} else {
			// Disable security when AUTH0_DOMAIN is not configured
			http.authorizeExchange(exchanges -> exchanges.anyExchange().permitAll());
		}

		return http.build();
	}

}

