package com.phuc.order.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class AuthenticationRequestInterceptor implements RequestInterceptor {

    @Value("${app.service-token:}")
    private String serviceToken;

    @Override
    public void apply(RequestTemplate template) {
        String authHeader = null;

        // First, try to get token from request header
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (servletRequestAttributes != null) {
            authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
        }

        // If no token from request, try to get from SecurityContext
        if (!StringUtils.hasText(authHeader)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
                Jwt jwt = (Jwt) authentication.getPrincipal();
                authHeader = "Bearer " + jwt.getTokenValue();
            }
        }

        // If still no token, use service token if configured
        if (!StringUtils.hasText(authHeader) && StringUtils.hasText(serviceToken)) {
            authHeader = "Bearer " + serviceToken;
        }

        // Add Authorization header if we have a token
        if (StringUtils.hasText(authHeader)) {
            template.header("Authorization", authHeader);
        }
        
        // Ensure Content-Type is set to application/json for POST/PUT requests with body
        if (template.body() != null && template.body().length > 0) {
            String method = template.method();
            if ("POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method)) {
                // Remove any existing Content-Type header and set to application/json
                template.removeHeader("Content-Type");
                template.header("Content-Type", "application/json");
            }
        }
    }

}


