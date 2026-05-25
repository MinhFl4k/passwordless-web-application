package com.app.demo.service;

import com.app.demo.dto.response.OtpResDto;

public interface OtpLoginService {

    String generateOtp(String email);

    OtpResDto validateOtp(String email, String otp);
}
