package com.app.demo.auth.password;

import com.app.demo.enums.ErrorMessage;
import com.app.demo.model.User;
import com.app.demo.repository.UserRepository;
import com.app.demo.service.LoginAttemptService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PasswordFailureHandler implements AuthenticationFailureHandler {

    private final UserRepository userRepository;
    private final LoginAttemptService loginAttemptService;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)
            throws IOException {

        HttpSession session = request.getSession();
        String email = request.getParameter("username");

        String errorMessage = ErrorMessage.INVALID_EMAIL_PASSWORD.getMessage();

        if (exception instanceof LockedException) {
            errorMessage = ErrorMessage.ACCOUNT_LOCKED.getMessage();
        }

        if (email != null && !email.isBlank()) {
            Optional<User> optionalUser = userRepository.findByEmail(email.trim());

            if (optionalUser.isPresent()) {
                User user = optionalUser.get();
                loginAttemptService.onPasswordFailure(user);

                if (loginAttemptService.isLocked(user)) {
                    errorMessage = ErrorMessage.ACCOUNT_LOCKED.getMessage();
                }
            }
        }

        session.setAttribute("FLASH_ERROR", errorMessage);

        response.sendRedirect("/login");
    }
}