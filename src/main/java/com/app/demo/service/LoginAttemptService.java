package com.app.demo.service;

import com.app.demo.model.User;

public interface LoginAttemptService {
    void onPasswordFailure(User user);

    void onOtpFailure(User user);

    void onLoginSuccess(User user);

    boolean isLocked(User user);
}
