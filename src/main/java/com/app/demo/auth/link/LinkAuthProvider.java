package com.app.demo.auth.link;

import com.app.demo.dto.common.CustomUserDetails;
import com.app.demo.service.UserTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LinkAuthProvider implements AuthenticationProvider {

    private final UserTokenService userTokenService;

    @Override
    public Authentication authenticate(Authentication authentication) {

        String rawToken = (String) authentication.getCredentials();

        CustomUserDetails userDetails =
                userTokenService.validateUserToken(rawToken);

        return new LinkAuthToken(
                userDetails,
                userDetails.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return LinkAuthToken.class.isAssignableFrom(authentication);
    }
}
