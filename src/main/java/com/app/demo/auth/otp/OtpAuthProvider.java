package com.app.demo.auth.otp;

import com.app.demo.dto.common.CustomUserDetails;
import com.app.demo.dto.response.OtpResDto;
import com.app.demo.enums.ErrorMessage;
import com.app.demo.model.User;
import com.app.demo.service.LoginAttemptService;
import com.app.demo.service.OtpLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class OtpAuthProvider implements AuthenticationProvider {

    private final OtpLoginService loginWithOtpService;

    private final UserDetailsService userDetailsService;

    private final LoginAttemptService loginAttemptService;

    private final UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();

    @Override
    public Authentication authenticate(Authentication authentication) {

        String email = authentication.getName();
        String otp = authentication.getCredentials().toString();

        CustomUserDetails userDetails;

        try {
            userDetails = (CustomUserDetails) userDetailsService.loadUserByUsername(email.trim());
        } catch (UsernameNotFoundException ex) {
            throw new BadCredentialsException(ErrorMessage.USER_NOT_FOUND.getMessage());
        }

        userDetailsChecker.check(userDetails);
        User user = userDetails.getUser();

        OtpResDto result = loginWithOtpService.validateOtp(email, otp);

        if (!result.isValid()) {
            loginAttemptService.onOtpFailure(user);

            if (loginAttemptService.isLocked(user)) {
                throw new LockedException(ErrorMessage.ACCOUNT_LOCKED.getMessage());
            }
            throw new BadCredentialsException(result.getMessage());
        }
        loginAttemptService.onLoginSuccess(user);

        OtpAuthToken authenticatedToken =
                new OtpAuthToken(userDetails, null, userDetails.getAuthorities());
        authenticatedToken.eraseCredentials();

        return authenticatedToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return OtpAuthToken.class.isAssignableFrom(authentication);
    }
}
