package com.app.demo.service.Impl;

import com.app.demo.dto.response.GeoLocationResDto;
import com.app.demo.service.GeoLocationService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GeoLocationServiceImpl implements GeoLocationService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public GeoLocationResDto resolve(String ipAddress) {
        try {
            if (ipAddress == null || ipAddress.isBlank()
                    || "127.0.0.1".equals(ipAddress)
                    || "0:0:0:0:0:0:0:1".equals(ipAddress)
                    || "::1".equals(ipAddress)) {
                return new GeoLocationResDto("LOCAL", "LOCAL", "LOCAL");
            }

            String url = "http://ip-api.com/json/" + ipAddress;
            IpApiResponse response = restTemplate.getForObject(url, IpApiResponse.class);

            if (response == null || !"success".equalsIgnoreCase(response.getStatus())) {
                return new GeoLocationResDto("UNKNOWN", "UNKNOWN", "UNKNOWN");
            }

            return new GeoLocationResDto(
                    defaultValue(response.getCountry()),
                    defaultValue(response.getRegionName()),
                    defaultValue(response.getCity())
            );
        } catch (Exception e) {
            return new GeoLocationResDto("UNKNOWN", "UNKNOWN", "UNKNOWN");
        }
    }

    private String defaultValue(String value) {
        return value == null || value.isBlank() ? "UNKNOWN" : value;
    }

    @Data
    public static class IpApiResponse {
        private String status;
        private String country;
        private String regionName;
        private String city;
    }
}
