package com.app.demo.auth.passkey;

import com.app.demo.service.SessionSecurityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.webauthn.authentication.WebAuthnAuthentication;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class PasskeySuccessHandler implements AuthenticationSuccessHandler {

    private final ObjectMapper objectMapper;

    private final SessionSecurityService sessionSecurityService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        if (!(authentication instanceof WebAuthnAuthentication webAuthnAuthentication)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unsupported authentication type");
            return;
        }

        String email = webAuthnAuthentication.getName();
        HttpSession session = request.getSession(false);

        sessionSecurityService.initializeSnapshot(email, request, session);
        if (session != null) {
            request.getSession().setAttribute("LOGIN_TIME", System.currentTimeMillis());
            session.removeAttribute("PASSKEY_LOGIN_EMAIL");
        }

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(
                objectMapper.writeValueAsString(
                        Map.of(
                                "authenticated", true,
                                "redirectUrl", "/home"
                        )
                )
        );
    }
}
