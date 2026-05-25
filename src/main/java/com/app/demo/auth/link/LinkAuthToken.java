package com.app.demo.auth.link;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.Collections;

public class LinkAuthToken extends AbstractAuthenticationToken {

    private final Object principal;
    private final String token;

    public LinkAuthToken(String token) {
        super(Collections.emptyList());
        this.principal = null;
        this.token = token;
        setAuthenticated(false);
    }

    public LinkAuthToken(
            Object principal,
            Collection<? extends GrantedAuthority> authorities
    ) {
        super(authorities);
        this.principal = principal;
        this.token = null;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }
}
