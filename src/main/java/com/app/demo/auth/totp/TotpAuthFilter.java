package com.app.demo.auth.totp;

import com.app.demo.enums.ErrorMessage;
import com.app.demo.service.SessionSecurityService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TotpAuthFilter extends UsernamePasswordAuthenticationFilter {

    private static final String POST = "POST";

    private final SessionSecurityService sessionSecurityService;

    @Override
    public Authentication attemptAuthentication(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws AuthenticationException {

        if (!POST.equalsIgnoreCase(request.getMethod())) {
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        String totpValue = request.getParameter("otp").trim();
        String email = request.getParameter("email").trim();

        if (email.isEmpty()) {
            throw new AuthenticationServiceException(ErrorMessage.EMAIL_REQUIRED.getMessage());
        }

        TotpAuthToken authRequest =
                new TotpAuthToken(email, totpValue);

        authRequest.setDetails(authenticationDetailsSource.buildDetails(request));

        return this.getAuthenticationManager().authenticate(authRequest);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authResult)
            throws IOException {

        request.changeSessionId();

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);

        HttpSessionSecurityContextRepository securityContextRepository =
                new HttpSessionSecurityContextRepository();
        securityContextRepository.saveContext(context, request, response);

        String email = authResult.getName();
        HttpSession session = request.getSession(false);

        if (session != null) {
            request.getSession().setAttribute("LOGIN_TIME", System.currentTimeMillis());
            sessionSecurityService.initializeSnapshot(email, request, session);
        }

        response.sendRedirect("/home");
    }
}
