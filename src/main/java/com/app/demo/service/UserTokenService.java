package com.app.demo.service;

import com.app.demo.dto.common.CustomUserDetails;
import com.app.demo.dto.response.UserVerifiedResDto;
import com.app.demo.enums.UserTokenType;
import jakarta.servlet.http.HttpServletRequest;

public interface UserTokenService {

    void sendUserTokenLink(String email, UserTokenType userTokenType);

    UserVerifiedResDto verifyWithUserToken(String token, HttpServletRequest request);

    CustomUserDetails validateUserToken(String tokenRequest);
}
