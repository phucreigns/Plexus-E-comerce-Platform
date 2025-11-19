package com.phuc.order.configuration;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class AuthenticationRequestInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        ServletRequestAttributes servletRequestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (servletRequestAttributes != null) {
            var authHeader = servletRequestAttributes.getRequest().getHeader("Authorization");
            if (StringUtils.hasText(authHeader)) {
                template.header("Authorization", authHeader);
            }
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


