package com.app.demo.auth.common;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String OTP_EMAIL_SESSION_KEY = "OTP_LOGIN_EMAIL";

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private static final List<String> LIMITED_URLS = List.of(
            "/login",
            "/otp-login-process",
            "/totp-login-process",
            "/signup"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();

        boolean isLimitedUrl = LIMITED_URLS.stream()
                .anyMatch(pattern -> pathMatcher.match(pattern, path));

        boolean isPost = "POST".equalsIgnoreCase(request.getMethod());

        return !isLimitedUrl || !isPost;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String bucketKey = resolveBucketKey(request);

        Bucket bucket = buckets.computeIfAbsent(bucketKey, key -> createBucket(request));

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/auth-limit-alert");
    }

    private Bucket createBucket(HttpServletRequest request) {
        String path = request.getServletPath();

        if (path.equals("/login")) {
            return Bucket.builder()
                    .addLimit(Bandwidth.classic(
                            5,
                            Refill.greedy(5, Duration.ofMinutes(1))
                    ))
                    .build();
        }

        if (path.equals("/otp-login-process")) {
            return Bucket.builder()
                    .addLimit(Bandwidth.classic(
                            5,
                            Refill.greedy(5, Duration.ofMinutes(1))
                    ))
                    .build();
        }

        if (path.equals("/totp-login-process")) {
            return Bucket.builder()
                    .addLimit(Bandwidth.classic(
                            5,
                            Refill.greedy(5, Duration.ofMinutes(1))
                    ))
                    .build();
        }

        if (path.equals("/signup")) {
            return Bucket.builder()
                    .addLimit(Bandwidth.classic(
                            3,
                            Refill.greedy(3, Duration.ofMinutes(1))
                    ))
                    .build();
        }

        return Bucket.builder()
                .addLimit(Bandwidth.classic(
                        10,
                        Refill.greedy(10, Duration.ofMinutes(1))
                ))
                .build();
    }

    private String resolveBucketKey(HttpServletRequest request) {
        String authTarget = resolveAuthUserOrIp(request);
        String urlGroup = resolveUrlGroup(request);

        return authTarget + ":" + urlGroup;
    }

    private String resolveAuthUserOrIp(HttpServletRequest request) {
        String path = request.getServletPath();

        if (path.equals("/login")) {
            String username = request.getParameter("username");

            if (username != null && !username.isBlank()) {
                return "PASSWORD_LOGIN:" + normalize(username)
                        + ":IP:" + getClientIp(request);
            }

            return "PASSWORD_LOGIN_IP:" + getClientIp(request);
        }

        if (path.equals("/totp-login-process")) {
            String email = request.getParameter("email");

            if (email != null && !email.isBlank()) {
                return "TOTP_LOGIN:" + normalize(email)
                        + ":IP:" + getClientIp(request);
            }

            return "TOTP_LOGIN_IP:" + getClientIp(request);
        }

        if (path.equals("/otp-login-process")) {
            String email = getOtpEmailFromSession(request);

            if (email != null && !email.isBlank()) {
                return "OTP_LOGIN:" + normalize(email)
                        + ":IP:" + getClientIp(request);
            }

            return "OTP_LOGIN_IP:" + getClientIp(request);
        }

        if (path.equals("/signup")) {
            return "SIGNUP_IP:" + getClientIp(request);
        }


        return "IP:" + getClientIp(request);
    }

    private String resolveUrlGroup(HttpServletRequest request) {
        String path = request.getServletPath();

        if (path.equals("/login")) {
            return "PASSWORD";
        }

        if (path.equals("/otp-login-process")) {
            return "OTP";
        }

        if (path.equals("/totp-login-process")) {
            return "TOTP";
        }

        if (path.equals("/signup")) {
            return "SIGNUP";
        }

        return "GENERAL";
    }

    private String getOtpEmailFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Object email = session.getAttribute(OTP_EMAIL_SESSION_KEY);

        if (email == null) {
            return null;
        }

        return email.toString();
    }

    private String normalize(String value) {
        return value.trim().toLowerCase();
    }

    private String getClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");

        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }
}
