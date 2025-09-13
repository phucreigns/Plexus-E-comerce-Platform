package com.phuc.order.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class CustomAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    private static final String AUTH0_PERMISSIONS = "permissions";
    private static final String AUTH0_ROLES = "https://xuanphuc.com/roles";

    @Override
    public Collection<GrantedAuthority> convert(@NonNull Jwt jwt) {
        List<String> permissions = jwt.getClaimAsStringList(AUTH0_PERMISSIONS);
        List<GrantedAuthority> authorities = permissions != null
                ? permissions.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList())
                : List.of();

        List<String> roles = jwt.getClaimAsStringList(AUTH0_ROLES);
        if (roles != null) {
            List<GrantedAuthority> roleAuthorities = roles.stream()
                    .map(role -> "ROLE_" + role)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            authorities.addAll(roleAuthorities);
        }

        return authorities;
    }

}