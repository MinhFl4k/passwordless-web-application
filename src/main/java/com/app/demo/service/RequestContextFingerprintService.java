package com.app.demo.service;

import jakarta.servlet.http.HttpServletRequest;

public interface RequestContextFingerprintService {
    String extractClientIp(HttpServletRequest request);

    String normalizeUserAgent(String userAgent);

    String sha256(String value);

    void buildSecurityEventContext(String email, HttpServletRequest request, String subject);
}
