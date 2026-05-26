package com.app.demo.service.Impl;

import com.app.demo.dto.response.OtpResDto;
import com.app.demo.enums.OtpStatus;
import com.app.demo.model.OtpCode;
import com.app.demo.repository.OtpCodeRepository;
import com.app.demo.service.OtpLoginService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OtpLoginServiceImpl implements OtpLoginService {

    private final OtpCodeRepository otpTokenRepository;

    private final PasswordEncoder passwordEncoder;

    @Value("${send.code.token.timeout}")
    private long SEND_CODE_TIMEOUT;

    @Transactional
    public String generateOtp(String email) {
        Optional<OtpCode> lastCreatedOtp = otpTokenRepository.findTopByEmailOrderByCreatedAtDesc(email);

        if (lastCreatedOtp.isPresent()) {
            OtpCode lastOtp = lastCreatedOtp.get();
            LocalDateTime allowedTime = lastOtp.getCreatedAt()
                    .plusSeconds(SEND_CODE_TIMEOUT);
            if (LocalDateTime.now().isBefore(allowedTime)) {
                throw new RuntimeException("Please wait " + SEND_CODE_TIMEOUT + " seconds then try again");
            }
        }

        otpTokenRepository.expireAllOldOtp(email);

        String otp = String.valueOf(new Random().nextInt(900000) + 100000);

        OtpCode token = new OtpCode();
        token.setEmail(email);
        token.setOtp(passwordEncoder.encode(otp));
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiryTime(LocalDateTime.now().plusMinutes(2));
        token.setUsed(false);

        otpTokenRepository.save(token);

        return otp;
    }

    @Override
    public OtpResDto validateOtp(String email, String otp) {
        Optional<OtpCode> tokenOtp =
                otpTokenRepository.findTopByEmailOrderByCreatedAtDesc(email);

        if (otp == null || !otp.matches("\\d{6}")) {
            return new OtpResDto(OtpStatus.INVALID);
        }

        if (tokenOtp.isEmpty())
            return new OtpResDto(OtpStatus.NOT_FOUND);

        OtpCode token = tokenOtp.get();

        if (token.isUsed())
            return new OtpResDto(OtpStatus.USED);
        if (token.getExpiryTime().isBefore(LocalDateTime.now()))
            return new OtpResDto(OtpStatus.EXPIRED);

        boolean otpMatch = passwordEncoder.matches(otp, token.getOtp());
        if (!otpMatch)
            return new OtpResDto(OtpStatus.INVALID);;

        token.setUsed(true);
        otpTokenRepository.save(token);

        return new OtpResDto(OtpStatus.VALID);
    }
}
