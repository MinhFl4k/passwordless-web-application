package com.app.demo.service;

import com.app.demo.dto.response.OtpResDto;

public interface TotpLoginService {
    String generateOTPProtocol(String email);

    String generateQRCode(String otpProtocol) throws Throwable;

    OtpResDto validateTotp(String secret, String totp);
}
