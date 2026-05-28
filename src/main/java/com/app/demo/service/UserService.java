package com.app.demo.service;

import com.app.demo.dto.request.ChangePasswordDto;
import com.app.demo.dto.request.UserSignupDto;
import com.app.demo.dto.request.UserUpdateDto;
import com.app.demo.dto.response.UserResDto;
import com.app.demo.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

public interface UserService {

    void updateAuthenticationPrincipal(User savedUser, Authentication authentication);

    void signupUser(
            UserSignupDto userSignupDto
    ) throws IllegalArgumentException;

    UserResDto processPostLogin(
            Authentication authentication
    );

    User getUserFromAuthentication(
            Authentication authentication
    );

    UserResDto getUserInfo(
            Authentication authentication
    );

    List<UserResDto> findAll();

    boolean updateUserInfo(
            Authentication authentication,
            UserUpdateDto userDto,
            HttpServletRequest request
    );

    void changePassword(
            String email,
            ChangePasswordDto changePasswordDTO,
            HttpServletRequest request
    );

    boolean checkCurrentPassword(
            String email,
            String currentPassword
    );

    UUID getLoggedInUserId(Authentication authentication);

    boolean isUserExist(String email);
}
