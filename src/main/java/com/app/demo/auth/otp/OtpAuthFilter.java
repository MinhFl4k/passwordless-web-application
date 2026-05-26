package com.app.demo.auth.otp;

import com.app.demo.enums.ErrorMessage;
import com.app.demo.service.SessionSecurityService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OtpAuthFilter extends UsernamePasswordAuthenticationFilter {

    private final SessionSecurityService sessionSecurityService;

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) {

        if (!request.getMethod().equals("POST")) {
            throw new AuthenticationServiceException(
                    "Unsupported authentication type: " + request.getMethod());
        }
        Object emailAttr = request.getSession().getAttribute("OTP_LOGIN_EMAIL");
        if (emailAttr == null) {
            throw new AuthenticationServiceException(ErrorMessage.EMAIL_REQUIRED.getMessage());
        }
        String email = emailAttr.toString().trim();

        String otp = request.getParameter("otp").trim();

        OtpAuthToken authRequest =
                new OtpAuthToken(email, otp);

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
            request.getSession().setAttribute("LOGIN_TIME", System.currentTimeMillis());
            sessionSecurityService.initializeSnapshot(email, request, session);
            session.removeAttribute("OTP_LOGIN_EMAIL");
        }

        response.sendRedirect("/home");
    }
}
