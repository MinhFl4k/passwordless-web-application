package com.app.demo.auth.password;

import com.app.demo.repository.UserRepository;
import com.app.demo.service.LoginAttemptService;
import com.app.demo.service.SessionSecurityService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class PasswordSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    private final LoginAttemptService loginAttemptService;

    private final SessionSecurityService sessionSecurityService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException {

        String email = authentication.getName();

        userRepository.findByEmail(email).ifPresent(loginAttemptService::onLoginSuccess);

        sessionSecurityService.initializeSnapshot(email, request, request.getSession());

        request.getSession().setAttribute("LOGIN_TIME", System.currentTimeMillis());

        response.sendRedirect("/home");
    }
}
