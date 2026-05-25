package com.app.demo.config;

import com.app.demo.auth.common.AbsoluteSessionTimeoutFilter;
import com.app.demo.auth.common.AccountLockCheckFilter;
import com.app.demo.auth.common.SessionIntegrityCheckFilter;
import com.app.demo.auth.link.LinkAuthFilter;
import com.app.demo.auth.link.LinkAuthProvider;
import com.app.demo.auth.link.LinkFailureHandler;
import com.app.demo.auth.passkey.PasskeySuccessHandler;
import com.app.demo.auth.password.PasswordFailureHandler;
import com.app.demo.auth.password.PasswordSuccessHandler;
import com.app.demo.auth.totp.TotpAuthFilter;
import com.app.demo.auth.totp.TotpAuthProvider;
import com.app.demo.auth.totp.TotpFailureHandler;
import com.app.demo.auth.otp.OtpAuthFilter;
import com.app.demo.auth.otp.OtpAuthProvider;
import com.app.demo.auth.otp.OtpFailureHandler;
import com.app.demo.service.CustomUserDetailService;
import com.app.demo.service.SessionSecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.webauthn.authentication.WebAuthnAuthenticationFilter;

@EnableMethodSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailService customUserDetailsService;

    private final OtpAuthProvider otpAuthProvider;

    private final TotpAuthProvider totpAuthProvider;

    private final LinkAuthProvider linkAuthProvider;

    private final OtpFailureHandler otpFailureHandler;

    private final TotpFailureHandler totpFailureHandler;

    private final LinkFailureHandler linkFailureHandler;

    private final PasskeySuccessHandler passkeySuccessHandler;

    private final PasswordFailureHandler passwordFailureHandler;

    private final PasswordSuccessHandler passwordSuccessHandler;

    private final AccountLockCheckFilter accountLockCheckFilter;

    private final SessionIntegrityCheckFilter sessionIntegrityCheckFilter;

    private final AbsoluteSessionTimeoutFilter absoluteSessionTimeoutFilter;

    private final SessionSecurityService sessionSecurityService;

    private final PasswordEncoder passwordEncoder;

    @Value("${app.url}")
    private String APP_URL;

    @Value("${app.full.url}")
    private String APP_FULL_URL;

    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            DaoAuthenticationProvider daoAuthenticationProvider) {
        return new ProviderManager(
                daoAuthenticationProvider,
                otpAuthProvider,
                totpAuthProvider,
                linkAuthProvider
        );
    }

    @Bean
    public OtpAuthFilter otpAuthFilter(AuthenticationManager authenticationManager) {
        OtpAuthFilter filter = new OtpAuthFilter(sessionSecurityService);
        filter.setAuthenticationManager(authenticationManager);
        filter.setFilterProcessesUrl("/otp-login-process");
        filter.setAuthenticationFailureHandler(otpFailureHandler);
        return filter;
    }

    @Bean
    public TotpAuthFilter totpAuthFilter(AuthenticationManager authenticationManager) {
        TotpAuthFilter filter = new TotpAuthFilter(sessionSecurityService);
        filter.setAuthenticationManager(authenticationManager);
        filter.setFilterProcessesUrl("/totp-login-process");
        filter.setAuthenticationFailureHandler(totpFailureHandler);
        return filter;
    }

    @Bean
    public LinkAuthFilter linkAuthFilter(AuthenticationManager authenticationManager) {
        LinkAuthFilter filter = new LinkAuthFilter(sessionSecurityService);
        filter.setAuthenticationManager(authenticationManager);
        filter.setFilterProcessesUrl("/auth/login");
        filter.setAuthenticationFailureHandler(linkFailureHandler);
        return filter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   OtpAuthFilter otpFilter,
                                                   TotpAuthFilter totpFilter,
                                                   LinkAuthFilter linkAuthFilter
    ) {
        http.authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/login",
                                "/login-with-otp",
                                "/login-with-totp",
                                "/login-with-link",
                                "/auth/**",
                                "/signup",
                                "/send-otp",
                                "/otp-login-process",
                                "/totp-login-process",
                                "/account-locked",
                                "/account-verified",
                                "/access-denied",
                                "/session-alert",
                                "/session-timeout",
                                "/css/**",
                                "/js/**"
                        ).permitAll()

                        .requestMatchers(
                                "/edit-profile",
                                "/change-password",
                                "/account-passkey",
                                "/passkeys/delete",
                                "/util/qrcode"
                        ).hasAnyRole("ADMIN", "USER")

                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedPage("/access-denied")
                )
                .addFilterBefore(otpFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(totpFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(linkAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(accountLockCheckFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(sessionIntegrityCheckFilter, AccountLockCheckFilter.class)
                .addFilterAfter(absoluteSessionTimeoutFilter, SessionIntegrityCheckFilter.class)
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(passwordSuccessHandler)
                        .failureHandler(passwordFailureHandler)
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/home",true)
                        .failureUrl("/login?error=true")
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .clearAuthentication(true)
                        .permitAll()
                )
                .requestCache(cache -> cache.disable())
                .webAuthn(webAuthn -> webAuthn
                        .rpId(APP_URL)
                        .allowedOrigins(APP_FULL_URL)
                        .withObjectPostProcessor(new ObjectPostProcessor<WebAuthnAuthenticationFilter>() {
                            @Override
                            public <O extends WebAuthnAuthenticationFilter> O postProcess(O filter) {
                                filter.setAuthenticationSuccessHandler(passkeySuccessHandler);
                                return filter;
                            }
                        })
                )
                .headers(headers -> headers
                        .cacheControl(cache -> {})
                )

                .userDetailsService(customUserDetailsService);

        return http.build();
    }
}
