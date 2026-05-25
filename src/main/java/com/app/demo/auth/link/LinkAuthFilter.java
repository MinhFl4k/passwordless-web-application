package com.app.demo.auth.link;

import com.app.demo.enums.ErrorMessage;
import com.app.demo.service.SessionSecurityService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class LinkAuthFilter extends UsernamePasswordAuthenticationFilter {

    private final SessionSecurityService sessionSecurityService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
    {
        String token = request.getParameter("token");

        if (token == null || token.isBlank()) {
            throw new BadCredentialsException(ErrorMessage.TOKEN_NOT_FOUND.getMessage());
        }

        LinkAuthToken authRequest =
                new LinkAuthToken(token);

        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult)
            throws IOException {

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);

        HttpSessionSecurityContextRepository securityContextRepository =
                new HttpSessionSecurityContextRepository();
        securityContextRepository.saveContext(context, request, response);

        String email = authResult.getName();
        HttpSession session = request.getSession(false);

        if (session != null) {
            sessionSecurityService.initializeSnapshot(email, request, session);
            request.getSession().setAttribute("LOGIN_TIME", System.currentTimeMillis());
        }

        response.sendRedirect("/home");
    }
}
