package com.app.demo.service.Impl;

import com.app.demo.model.User;
import com.app.demo.repository.UserRepository;
import com.app.demo.service.LoginAttemptService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LoginAttemptServiceImpl implements LoginAttemptService {

    private static final int MAX_PASSWORD_ATTEMPTS = 5;
    private static final int MAX_OTP_ATTEMPTS = 5;
    private static final long LOCK_MINUTES = 30;

    private final UserRepository userRepository;

    @Override
    public void onPasswordFailure(User user) {
        int current = user.getPasswordFailedAttempts() == null ? 0 : user.getPasswordFailedAttempts();
        current++;
        user.setPasswordFailedAttempts(current);

        if (current >= MAX_PASSWORD_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
        }

        userRepository.save(user);
    }

    @Override
    public void onOtpFailure(User user) {
        int current = user.getOtpFailedAttempts() == null ? 0 : user.getOtpFailedAttempts();
        current++;
        user.setOtpFailedAttempts(current);

        if (current >= MAX_OTP_ATTEMPTS) {
            user.setLockedUntil(LocalDateTime.now().plusMinutes(LOCK_MINUTES));
        }

        userRepository.save(user);
    }

    @Override
    public void onLoginSuccess(User user) {
        user.setPasswordFailedAttempts(0);
        user.setOtpFailedAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);
    }

    @Override
    public boolean isLocked(User user) {
        return user.getLockedUntil() != null
                && user.getLockedUntil().isAfter(LocalDateTime.now());
    }
}
