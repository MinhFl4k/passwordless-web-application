package com.app.demo.service.Impl;

import com.app.demo.dto.request.EmailContextDto;
import com.app.demo.dto.response.GeoLocationResDto;
import com.app.demo.service.EmailService;
import com.app.demo.service.GeoLocationService;
import com.app.demo.service.RequestContextFingerprintService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Service
@RequiredArgsConstructor
public class RequestContextFingerprintServiceImpl implements RequestContextFingerprintService {

    private final GeoLocationService geoLocationService;
    private final UserAgentAnalyzer uaa;
    private final EmailService emailService;

    @Override
    public String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @Override
    public String normalizeUserAgent(String userAgent) {
        return StringUtils.hasText(userAgent) ? userAgent.trim() : "UNKNOWN";
    }

    @Override
    public String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Cannot hash login id", e);
        }
    }

    @Override
    public void buildSecurityEventContext(String email, HttpServletRequest request, String subject) {
        String ip = extractClientIp(request);
        String uaString = normalizeUserAgent(request.getHeader("User-Agent"));
        GeoLocationResDto geo = geoLocationService.resolve(ip);
        UserAgent userAgent = uaa.parse(uaString);

        EmailContextDto context = EmailContextDto.builder()
                .ip(ip)
                .country(geo != null ? geo.getCountry() : null)
                .city(geo != null ? geo.getCity() : null)
                .osName(userAgent.getValue("OperatingSystemName"))
                .browserName(userAgent.getValue("AgentName"))
                .device(userAgent.getValue("DeviceClass"))
                .build();

        emailService.sendSecurityEventContext(email, context, subject);
    }
}
