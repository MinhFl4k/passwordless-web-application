package com.app.demo.service;

import com.app.demo.dto.response.GeoLocationResDto;

public interface GeoLocationService {
    GeoLocationResDto resolve(String ipAddress);
}
