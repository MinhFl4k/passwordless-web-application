package com.app.demo.auth.common;

import com.app.demo.service.SessionSecurityService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class SessionIntegrityCheckFilter extends OncePerRequestFilter {

    private final SessionSecurityService sessionSecurityService;

    private static final Set<String> SENSITIVE_URLS = Set.of(
            "/account-passkey",
            "/change-password",
            "/departments/**",
            "/edit-profile",
            "/home",
            "/list",
            "/passkeys/delete",
            "/util/qrcode"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return !SENSITIVE_URLS.contains(uri);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {

            String email = authentication.getName();

            HttpSession session = request.getSession(false);

            boolean changed = sessionSecurityService.isContextChanged(
                    email,
                    request,
                    session
            );

            if (changed) {
                new SecurityContextLogoutHandler().logout(request, response, authentication);
                SecurityContextHolder.clearContext();
                response.sendRedirect(request.getContextPath() + "/session-alert");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
