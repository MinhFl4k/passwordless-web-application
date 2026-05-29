package com.app.demo.controller;

import com.app.demo.service.TotpLoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/util")
@PreAuthorize("hasAnyRole('ADMIN','USER')")
public class UtilController {

    private final TotpLoginService totpService;

    @GetMapping(value = "/qrcode")
    public String generateQRCode(Authentication authentication) throws Throwable {
        String userName = authentication.getName();
        String otpProtocol = totpService.generateOTPProtocol(userName);
        return totpService.generateQRCode(otpProtocol);
    }
}
