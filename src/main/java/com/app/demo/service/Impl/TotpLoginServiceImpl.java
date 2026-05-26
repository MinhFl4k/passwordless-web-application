package com.app.demo.service.Impl;

import com.app.demo.dto.response.OtpResDto;
import com.app.demo.enums.ErrorMessage;
import com.app.demo.enums.OtpStatus;
import com.app.demo.model.User;
import com.app.demo.repository.UserRepository;
import com.app.demo.service.TotpLoginService;
import com.app.demo.util.TotpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class TotpLoginServiceImpl implements TotpLoginService {

    private final UserRepository userRepository;

    @Value("${totp.application.name}")
    private String SECRET;

    @Value("${totp.application.time}")
    private String TIME;

    @Value("${totp.application.url}")
    private String TOTP_APP_URL;

    @Override
    public String generateOTPProtocol(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(ErrorMessage.USER_NOT_FOUND.getMessage()));

        String issuer = SECRET;
        String totpSecret = user.getSecret().trim();

        String encodedIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8);
        String encodedEmail = URLEncoder.encode(email, StandardCharsets.UTF_8);

        return String.format(
                TOTP_APP_URL,
                encodedIssuer,
                encodedEmail,
                totpSecret,
                encodedIssuer
        );
    }

    @Override
    public String generateQRCode(String otpProtocol) throws Throwable {
        return TotpUtil.generateQRCode(otpProtocol);
    }

    @Override
    public OtpResDto validateTotp(String secret, String totp) {

        if (!StringUtils.hasText(secret)) {
            return new OtpResDto(OtpStatus.INVALID);
        }

        if (totp == null) {
            return new OtpResDto(OtpStatus.NOT_FOUND);
        }

        if (!totp.matches("\\d{6}")) {
            return new OtpResDto(OtpStatus.INVALID);
        }

        try {
            if (!TotpUtil.verifyCode(secret, totp, Integer.parseInt(TIME))) {
                return new OtpResDto(OtpStatus.INVALID);
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new InternalAuthenticationServiceException(e.getMessage());
        }

        return new OtpResDto(OtpStatus.VALID);
    }
}
