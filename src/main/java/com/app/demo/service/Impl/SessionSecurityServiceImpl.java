package com.app.demo.service.Impl;

import com.app.demo.dto.response.GeoLocationResDto;
import com.app.demo.dto.session.SessionSecuritySnapshotDto;
import com.app.demo.enums.SessionKey;
import com.app.demo.model.User;
import com.app.demo.repository.UserRepository;
import com.app.demo.service.GeoLocationService;
import com.app.demo.service.RequestContextFingerprintService;
import com.app.demo.service.SessionSecurityService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SessionSecurityServiceImpl implements SessionSecurityService {

    private final UserRepository userRepository;
    private final GeoLocationService geoLocationService;
    private final RequestContextFingerprintService requestContextFingerprintService;

    @Override
    public void initializeSnapshot(String email, HttpServletRequest request, HttpSession session) {
        if (session == null || !StringUtils.hasText(email)) {
            return;
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return;
        }

        String userAgent = requestContextFingerprintService.normalizeUserAgent(request.getHeader("User-Agent"));
        String ip = requestContextFingerprintService.extractClientIp(request);
        GeoLocationResDto geo = geoLocationService.resolve(ip);

        SessionSecuritySnapshotDto snapshot = SessionSecuritySnapshotDto.builder()
                .userId(user.getId())
                .email(user.getEmail())
                .userAgent(userAgent)
                .ip(ip)
                .country(geo.getCountry())
                .city(geo.getCity())
                .issuedAt(LocalDateTime.now())
                .build();

        session.setAttribute(SessionKey.SESSION_SECURITY_SNAPSHOT.getMessage(), snapshot);
    }

    @Override
    public boolean isContextChanged(String email, HttpServletRequest request, HttpSession session) {
        if (session == null || !StringUtils.hasText(email)) {
            return false;
        }

        Object obj = session.getAttribute(SessionKey.SESSION_SECURITY_SNAPSHOT.getMessage());
        if (!(obj instanceof SessionSecuritySnapshotDto snapshot)) {
            return false;
        }

        String currentUserAgent = requestContextFingerprintService.normalizeUserAgent(request.getHeader("User-Agent"));
        String currentIp = requestContextFingerprintService.extractClientIp(request);
        GeoLocationResDto geo = geoLocationService.resolve(currentIp);

        boolean userAgentChange = !Objects.equals(snapshot.getUserAgent(), currentUserAgent);
        boolean ipChanged = !Objects.equals(snapshot.getIp(), currentIp);
        boolean countryChanged = !Objects.equals(snapshot.getCountry(), geo.getCountry());
        boolean cityChanged = !Objects.equals(snapshot.getCity(), geo.getCity());

        return userAgentChange || ipChanged || countryChanged || cityChanged;
    }
}