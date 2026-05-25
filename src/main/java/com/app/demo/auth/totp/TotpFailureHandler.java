package com.app.demo.auth.totp;

import com.app.demo.enums.ErrorMessage;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TotpFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception)

    throws IOException {

        String errorMessage = exception.getMessage();

        if (exception instanceof LockedException)
            errorMessage = ErrorMessage.ACCOUNT_LOCKED.getMessage();

        HttpSession session = request.getSession();
        session.setAttribute("FLASH_ERROR", errorMessage);

        response.sendRedirect("/login-with-totp");
    }
}
