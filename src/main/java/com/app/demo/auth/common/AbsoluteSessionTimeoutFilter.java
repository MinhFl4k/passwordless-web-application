package com.app.demo.auth.common;

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

@Component
@RequiredArgsConstructor
public class AbsoluteSessionTimeoutFilter extends OncePerRequestFilter {

    private static final long ABSOLUTE_TIMEOUT_MILLIS = 60 * 60 * 1000;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        HttpSession session = request.getSession(false);

        boolean loggedIn = authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);

        if (loggedIn && session != null) {
            Long loginTime = (Long) session.getAttribute("LOGIN_TIME");

            if (loginTime != null) {
                long now = System.currentTimeMillis();

                if (now - loginTime >= ABSOLUTE_TIMEOUT_MILLIS) {
                    new SecurityContextLogoutHandler().logout(request, response, authentication);
                    response.sendRedirect(request.getContextPath() + "/session-timeout");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }
}
